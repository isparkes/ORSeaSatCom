/*
 * Copyright 2006-2026 Ian Sparkes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package SeaSatCom;

import OpenRate.process.AbstractRegexMatch;
import OpenRate.record.IRecord;

/**
 * This class looks up the name of any bundles that we have to use.
 */
public class FriendsFamily
        extends AbstractRegexMatch {

  // this is used for the lookup
  String[] tmpSearchParameters = new String[2];

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public IRecord procValidRecord(IRecord r) {
    SeaSatRecord CurrentRecord;
    String RegexGroup;
    String RegexResult;
    int Index;

    CurrentRecord = (SeaSatRecord) r;

    if (CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE) {
      // Run through all of the options, plus the base tariff looking for
      // bundles
      for (Index = 0; Index < CurrentRecord.Options.size(); Index++) {
        // look for option based discounts
        RegexGroup = CurrentRecord.GuidingKey;

        // Prepare the paramters to perform the search on
        tmpSearchParameters[0] = CurrentRecord.Options.get(Index);
        tmpSearchParameters[1] = CurrentRecord.Norm_B_Number;

        // Get the discount to apply to this CDR
        RegexResult = getRegexMatch(RegexGroup, tmpSearchParameters);

        if (!RegexResult.equalsIgnoreCase("nomatch")) {
          if (!RegexResult.equalsIgnoreCase("none")) {
            CurrentRecord.DiscountGranted = -CurrentRecord.Quantity;
            CurrentRecord.UsedDiscount = CurrentRecord.Options.get(Index);
            CurrentRecord.DiscountRule = CurrentRecord.Options.get(Index);
            CurrentRecord.DiscountRUM = "DUR";
            CurrentRecord.Quantity = 0;
            CurrentRecord.updateRUMValue("DUR", -CurrentRecord.getRUMValue("DUR"));

            // found the discount, get out
            break;
          }
        }
      }
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
