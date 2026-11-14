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
import OpenRate.record.IError;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;
import java.util.ArrayList;

/**
 * This class is an example of a plug in that does only a lookup, and thus does
 * not need to be registered as transaction bound. Recall that we will only need
 * to be transaction aware when we need some specific information from the
 * transaction management (e.g. the base file name) or when we require to have
 * the possibility to undo transaction work in the case of some failure.
 *
 * In this case we do not need it, as the input and output adapters will roll
 * the information back for us (by removing the output stream) in the case of an
 * error.
 */
public class SATDestinationLookup extends AbstractRegexMatch {

  // this is used for the lookup

  String[] tmpSearchParameters = new String[1];

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public IRecord procValidRecord(IRecord r) {

    IError tmpError;
    SeaSatRecord CurrentRecord = (SeaSatRecord) r;
    ArrayList<String> Results = null;

    // We only transform the basic records, and leave the others alone
    if (CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE) {
      if (CurrentRecord.ProductService.equals("SSMS")) {
        // SMS - set to a default value
        CurrentRecord.DestinationZone = "NONE";
      } else {
        tmpSearchParameters[0] = CurrentRecord.Description;
        try {
          Results = getRegexMatchWithChildData(CurrentRecord.getChargePacket(0).zoneModel, tmpSearchParameters);
        } catch (ArrayIndexOutOfBoundsException aiobe) {
          tmpError = new RecordError("ERR_DESTINATION_INVALID", ErrorType.SPECIAL);
          CurrentRecord.addError(tmpError);
        }

        // Check we have some results
        if (Results != null) {
          if (Results.size() > 1) {
            if (!Results.get(0).equalsIgnoreCase("nomatch")) {
              // get the monde plus origin zone
              CurrentRecord.DestinationZone = Results.get(0);

              // Get the description
              CurrentRecord.DestinationDescription = Results.get(1);

              // We need to put it in the Charge Packet as well
              CurrentRecord.getChargePacket(0).zoneResult = CurrentRecord.DestinationZone;
            } else {
              tmpError = new RecordError("ERR_DESTINATION_LOOKUP", ErrorType.SPECIAL);
              CurrentRecord.addError(tmpError);
            }
          } else {
            tmpError = new RecordError("ERR_DESTINATION_LOOKUP", ErrorType.SPECIAL);
            CurrentRecord.addError(tmpError);
          }
        } else {
          tmpError = new RecordError("ERR_DESTINATION_LOOKUP", ErrorType.SPECIAL);
          CurrentRecord.addError(tmpError);
        }
      }
    } else {
      // Terminating - set to a default value
      CurrentRecord.DestinationZone = "NONE";
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
