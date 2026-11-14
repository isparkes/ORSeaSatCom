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

import OpenRate.process.AbstractStubPlugIn;
import OpenRate.record.BalanceImpact;
import OpenRate.record.ChargePacket;
import OpenRate.record.IRecord;
import OpenRate.utils.ConversionUtils;

/**
 * This module rolls up the charges from the individual charge packets to give a
 * grand total charge.
 *
 * @author Afzaal
 */
public class Rollup extends AbstractStubPlugIn {

  ConversionUtils conversions;

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public IRecord procValidRecord(IRecord r) {
    ChargePacket tmpCP;
    BalanceImpact tmpBI;
    SeaSatRecord CurrentRecord = (SeaSatRecord) r;
    int IndexCP;
    int IndexBI;

    // accumulate the charge for all of the charge packets
    for (IndexCP = 0; IndexCP < CurrentRecord.getChargePacketCount(); IndexCP++) {
      tmpCP = CurrentRecord.getChargePacket(IndexCP);

      // get only the traffic charges, ignoring the pack usage charges
      if (tmpCP.packetType.equals("R")) {
        // Roll up the rated charge
        CurrentRecord.RatedAmount += tmpCP.chargedValue;

        // roll up the discounted information
        for (IndexBI = 0; IndexBI < CurrentRecord.getBalanceImpactCount(); IndexBI++) {
          tmpBI = CurrentRecord.getBalanceImpact(IndexBI);

          // Skip over creation impacts
          if (tmpBI.ruleName.equals("CREATION")) {
            // do nothing for the moment
          } else {
            CurrentRecord.DiscountGranted += tmpBI.balanceDelta;
          }
        }
      }
    }

    // Do rounding
    CurrentRecord.RatedAmount = conversions.getRoundedValue(CurrentRecord.RatedAmount, 2);
    CurrentRecord.DiscountGranted = conversions.getRoundedValue(CurrentRecord.DiscountGranted, 2);

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
