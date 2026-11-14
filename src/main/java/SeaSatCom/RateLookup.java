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
import OpenRate.process.AbstractRUMRateCalc;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;

/**
 * This class looks up the price model to use, based on the roaming scenario
 * (the caller was roaming or on the domestic network), the destination of the
 * call, and the direction of the call (originating or terminating).
 */
public class RateLookup extends AbstractRUMRateCalc
{
  
  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  
  @Override
  public IRecord procValidRecord(IRecord r)
  {
    RecordError tmpError;
    SeaSatRecord CurrentRecord = (SeaSatRecord) r;
    CurrentRecord.createBreakdown = true;

    if ((CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE) |
        (CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE))
    {
      try
      {
        performRating(CurrentRecord);
      }
      catch (ProcessingException pe)
      {
        tmpError = new RecordError("ERR_RATE_LOOKUP", ErrorType.SPECIAL);
        CurrentRecord.addError(tmpError);
      }

      // Get the impact to write into the output
      //CurrentRecord.RatedAmount = CurrentRecord.getTotalImpact("EUR");
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r)
  {
      return r;
  }
}
