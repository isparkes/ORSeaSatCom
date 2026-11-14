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
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;
import java.util.ArrayList;

/**
 * This module looks up the origin zone based on the origin, which was
 * previously calculated.
 *
 */
public class OriginLookup
        extends AbstractRegexMatch {

  // this is used for the lookup

  String[] tmpSearchParameters = new String[1];

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public IRecord procValidRecord(IRecord r) {
    String RegexGroup;
    SeaSatRecord CurrentRecord;
    ArrayList<String> Results;

    CurrentRecord = (SeaSatRecord) r;
    RecordError tmpError;

    if (CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE) {
      // Prepare the paramters to perform the search on
      tmpSearchParameters[0] = CurrentRecord.Origin;

      RegexGroup = CurrentRecord.service;

      Results = getRegexMatchWithChildData(RegexGroup, tmpSearchParameters);

      // Check we have some results
      if ((Results != null) & (Results.size() > 1)) {
        if (!Results.get(0).equalsIgnoreCase("nomatch")) {
          if (CurrentRecord.PassportCall) {
            // get the passport origin zone
            CurrentRecord.OriginZone = Results.get(1);
          } else {
            // get the monde plus origin zone
            CurrentRecord.OriginZone = Results.get(0);
          }

          // Get the description
          CurrentRecord.OriginDescription = Results.get(2);
        } else {
          tmpError = new RecordError("ERR_ORIGIN_LOOKUP", ErrorType.SPECIAL);
          CurrentRecord.addError(tmpError);
        }
      } else {
        tmpError = new RecordError("ERR_ORIGIN_LOOKUP", ErrorType.SPECIAL);
        CurrentRecord.addError(tmpError);
      }
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }

  // -----------------------------------------------------------------------------
  // ------------------------ Start of custom functions --------------------------
  // -----------------------------------------------------------------------------
}
