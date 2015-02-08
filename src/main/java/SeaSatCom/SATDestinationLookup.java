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
