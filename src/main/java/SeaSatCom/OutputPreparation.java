/* ====================================================================
 * Limited Evaluation License:
 *
 * The exclusive owner of this work is Tiger Shore Management Ltd.
 * This work, including all associated documents and components
 * is Copyright Tiger Shore Management Ltd 2006-2008.
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
