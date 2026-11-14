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

import OpenRate.exception.ProcessingException;
import OpenRate.process.AbstractRUMTimeMatch;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;

/**
 * This class is a bit of a fat Filter, doing all of the Pollux logic evaluation
 * and selection in one go. It uses information from three cache objects to
 * evaluate the logic candidate to use for the rating
 */
public class TimeLookup extends AbstractRUMTimeMatch {
  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------

  @Override
  public IRecord procValidRecord(IRecord r) {
    RecordError tmpError;
    SeaSatRecord CurrentRecord = (SeaSatRecord) r;

    // We only transform the SAT records
    if (CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE) {
      // pull over the tariff, as this is the trigger for the time lookup
      CurrentRecord.getChargePacket(0).timeModel = CurrentRecord.UserTariff;

      // Do the lookup
      try {
        performRUMTimeMatch(CurrentRecord);
      } catch (ProcessingException pe) {
        tmpError = new RecordError("ERR_TIME_LOOKUP", ErrorType.SPECIAL);
        CurrentRecord.addError(tmpError);
      }
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
