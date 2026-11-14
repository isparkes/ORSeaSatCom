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

/**
 * This module handles the records which arrive according to whether they are
 * good records or error records.
 *
 * Good records are unmapped and sent to the jBilling and balance pipe outputs.
 *
 * Error records are send to the suspense output depending on the type of error
 * they have.
 *
 * @author afzaal
 */
public class OutputPreparation
        extends AbstractRegexMatch {

  // this is used for the lookup

  String[] tmpSearchParameters = new String[1];
  public static final String JBILLING_OUTPUT = "jbillingOutput";
  public static final String BALANCE_OUTPUT = "BalanceOutput";

  @Override
  public IRecord procValidRecord(IRecord r) {
    SeaSatRecord CurrentRecord = (SeaSatRecord) r;

    // set the good output
    CurrentRecord.addOutput(JBILLING_OUTPUT);
    CurrentRecord.addOutput(BALANCE_OUTPUT);

    // for valid records
    if (CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE) {
      // get the descriptions
      CurrentRecord.mapJBDescriptionGSM();

      //get the category
      CurrentRecord.mapJBCategoryGSM();
    } else if (CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE) {
      // get the descriptions
      CurrentRecord.mapJBDescriptionSAT();

      //get the category
      CurrentRecord.mapJBCategorySAT();
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    String RegexGroup;
    String RegexResult;
    SeaSatRecord CurrentRecord;
    RecordError tmpError;
    String ErrorDescription;

    CurrentRecord = (SeaSatRecord) r;

    if ((CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE)
            | (CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE)) {
      // see if the record has errors
      if (CurrentRecord.getErrorCount() > 0) {
        // 	if so, get the first error
        ErrorDescription = CurrentRecord.getErrors().get(0).getMessage();

        // 	Prepare the paramters to perform the search on
        tmpSearchParameters[0] = ErrorDescription;

        RegexGroup = "Default";
        RegexResult = getRegexMatch(RegexGroup, tmpSearchParameters);

        if (!RegexResult.equalsIgnoreCase("nomatch")) {
          CurrentRecord.addOutput(RegexResult);
        } else {
          CurrentRecord.addOutput("Error");
          tmpError = new RecordError("ERR_SUSPENSE_LOOKUP", ErrorType.SPECIAL, getSymbolicName());
          CurrentRecord.addError(tmpError);
        }

      }

    }

    return r;
  }
}
