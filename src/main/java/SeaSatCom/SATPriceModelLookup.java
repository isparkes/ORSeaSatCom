/* ====================================================================
 * Limited Evaluation License:
 *
 * The exclusive owner of this work is Tiger Shore Management Ltd.
 * This work, including all associated documents and components
 * is Copyright Tiger Shore Management Ltd 2006-2010.
 *
 * The following restrictions apply unless they are expressly relaxed in a
 * contractual agreement between the license holder or one of its officially
 * assigned agents and you or your organisation:
 *
 * 1) This work may not be disclosed, either in full or in part, in any form
 *    electronic or physical, to any third party. This includes both in the
 *    form of source code and compiled modules.
 * 2) This work contains trade secrets in the form of architecture, algorithms
 *    methods and technologies. These trade secrets may not be disclosed to
 *    third parties in any form, either directly or in summary or paraphrased
 *    form, nor may these trade secrets be used to construct products of a
 *    similar or competing nature either by you or third parties.
 * 3) This work may not be included in full or in part in any application.
 * 4) You may not remove or alter any proprietary legends or notices contained
 *    in or on this work.
 * 5) This software may not be reverse-engineered or otherwise decompiled, if
 *    you received this work in a compiled form.
 * 6) This work is licensed, not sold. Possession of this software does not
 *    imply or grant any right to you.
 * 7) You agree to disclose any changes to this work to the copyright holder
 *    and that the copyright holder may include any such changes at its own
 *    discretion into the work
 * 8) You agree not to derive other works from the trade secrets in this work,
 *    and that any such derivation may make you liable to pay damages to the
 *    copyright holder
 * 9) You agree to use this software exclusively for evaluation purposes, and
 *    that you shall not use this software to derive commercial profit or
 *    support your business or personal activities.
 *
 * This software is provided "as is" and any expressed or impled warranties,
 * including, but not limited to, the impled warranties of merchantability
 * and fitness for a particular purpose are disclaimed. In no event shall
 * Tiger Shore Management or its officially assigned agents be liable to any
 * direct, indirect, incidental, special, exemplary, or consequential damages
 * (including but not limited to, procurement of substitute goods or services;
 * Loss of use, data, or profits; or any business interruption) however caused
 * and on theory of liability, whether in contract, strict liability, or tort
 * (including negligence or otherwise) arising in any way out of the use of
 * this software, even if advised of the possibility of such damage.
 * This software contains portions by The Apache Software Foundation, Robert
 * Half International.
 * ====================================================================
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
