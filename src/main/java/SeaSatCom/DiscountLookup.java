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
public class DiscountLookup
        extends AbstractRegexMatch {

  // this is used for the lookup

  String[] tmpSearchParameters = new String[4];

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

    if ((CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE)
            | (CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE)) {
      // Run through all of the options, plus the base tariff looking for
      // bundles
      for (Index = 0; Index < CurrentRecord.Options.size() + 1; Index++) {
        if (Index == CurrentRecord.Options.size()) {
          // Look for the tariff based discounts
          RegexGroup = "Tariff";
        } else {
          // look for option based discounts
          RegexGroup = CurrentRecord.Options.get(Index);
        }

        // Prepare the paramters to perform the search on
        tmpSearchParameters[0] = CurrentRecord.UserTariff;
        tmpSearchParameters[1] = CurrentRecord.OriginZone;
        tmpSearchParameters[2] = CurrentRecord.DestinationZone;
        tmpSearchParameters[3] = CurrentRecord.CDRType;

        // Get the discount to apply to this CDR
        RegexResult = getRegexMatch(RegexGroup, tmpSearchParameters);

        if (!RegexResult.equalsIgnoreCase("nomatch")) {
          if (RegexResult.equalsIgnoreCase("none")) {
            CurrentRecord.UsedDiscount = "";
            CurrentRecord.DiscountOption = "";
          } else {
            CurrentRecord.UsedDiscount = RegexResult;
            CurrentRecord.DiscountOption = RegexGroup;

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
