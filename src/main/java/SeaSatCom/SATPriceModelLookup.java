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
import OpenRate.record.ChargePacket;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;
import OpenRate.record.TimePacket;

/**
 * This class looks up the price model to use, based on the roaming scenario
 * (the caller was roaming or on the domestic network), the destination of the
 * call, and the direction of the call (originating or terminating).
 */
public class SATPriceModelLookup
        extends AbstractRegexMatch {

  // String array used for searching
  private final String[] tmpSearchParameters = new String[3];

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public IRecord procValidRecord(IRecord r) {
    String RegexGroup;
    String RegexResult;
    RecordError tmpError;

    SeaSatRecord CurrentRecord = (SeaSatRecord) r;

    if (CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE) {
      for (ChargePacket tmpCP : CurrentRecord.getChargePackets()) {
        for (TimePacket tmpTZ : tmpCP.getTimeZones()) {
          // Prepare the paramters to perform the search on
          tmpSearchParameters[0] = tmpCP.zoneResult;
          tmpSearchParameters[1] = CurrentRecord.UserTariff;
          tmpSearchParameters[2] = tmpTZ.timeResult;

          // the group is the rate sheet
          RegexGroup = CurrentRecord.PriceSheet;

          // Look up the rate model to use
          RegexResult = getRegexMatch(RegexGroup, tmpSearchParameters);
          if (RegexResult.equals("NOMATCH")) {
            tmpError = new RecordError("ERR_PRICE_MODEL_NOT_FOUND", ErrorType.SPECIAL);
            CurrentRecord.addError(tmpError);
          }

          tmpTZ.priceGroup = RegexResult;

          // See if we need to add something for pack rating
          if (CurrentRecord.PackUsageFlag == 1) {
            // add a new charge packet
            ChargePacket PackCP = new ChargePacket();
            PackCP.packetType = "P";

            tmpSearchParameters[0] = CurrentRecord.OriginZone;
            tmpSearchParameters[1] = CurrentRecord.DestinationZone;
            tmpSearchParameters[2] = CurrentRecord.UserTariff;

            // the group is the rate sheet
            RegexGroup = CurrentRecord.DiscountOption;

            // Look up the rate model to use
            RegexResult = getRegexMatch(RegexGroup, tmpSearchParameters);
            if (RegexResult.equals("NOMATCH")) {
              tmpError = new RecordError("ERR_PACK_PRICE_MODEL_NOT_FOUND", ErrorType.SPECIAL);
              CurrentRecord.addError(tmpError);
            }

            tmpTZ.priceGroup = RegexResult;
            CurrentRecord.addChargePacket(PackCP);
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
