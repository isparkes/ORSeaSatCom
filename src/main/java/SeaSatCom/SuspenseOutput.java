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
 * and fitness for a particular purpose are discplaimed. In no event shall
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

import OpenRate.adapter.file.FlatFileOutputAdapter;
import OpenRate.record.FlatRecord;
import OpenRate.record.IRecord;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The Output Adapter is responsible for writing the suspense records to the
 * target file.
 *
 * @author afzaal
 */
public class SuspenseOutput
        extends FlatFileOutputAdapter {

  @Override
  public Collection<IRecord> procValidRecord(IRecord r) {
    String ERROR_CODE;
    int RECYCLE_COUNT;
    Collection<IRecord> Outbatch;
    Outbatch = new ArrayList<>();
    FlatRecord tmpOutRecord = new FlatRecord();
    SeaSatRecord tmpInRecord = (SeaSatRecord) r;

    ERROR_CODE = "NO_ERROR";
    tmpInRecord.Recycle_Count++;
    RECYCLE_COUNT = tmpInRecord.Recycle_Count;

    StringBuilder recordData = new StringBuilder(SeaSatRecord.RECYCLE_TAG);
    recordData.append(SeaSatRecord.FIELD_SPLITTER);
    recordData.append(ERROR_CODE);
    recordData.append(SeaSatRecord.FIELD_SPLITTER);
    recordData.append(RECYCLE_COUNT);
    recordData.append(SeaSatRecord.FIELD_SPLITTER);
    recordData.append(tmpInRecord.unmapOriginalData());

    tmpOutRecord.setData(recordData.toString());
    Outbatch.add(tmpOutRecord);

    return Outbatch;
  }

  @Override
  public Collection<IRecord> procErrorRecord(IRecord r) {
    String ERROR_CODE;
    int RECYCLE_COUNT;
    Collection<IRecord> Outbatch;
    Outbatch = new ArrayList<>();
    FlatRecord tmpOutRecord = new FlatRecord();
    SeaSatRecord tmpInRecord = (SeaSatRecord) r;

    ERROR_CODE = tmpInRecord.getErrors().get(0).getMessage();
    tmpInRecord.Recycle_Count++;
    RECYCLE_COUNT = tmpInRecord.Recycle_Count;

    StringBuilder recordData = new StringBuilder(SeaSatRecord.RECYCLE_TAG);
    recordData.append(SeaSatRecord.FIELD_SPLITTER);
    recordData.append(ERROR_CODE);
    recordData.append(SeaSatRecord.FIELD_SPLITTER);
    recordData.append(RECYCLE_COUNT);
    recordData.append(SeaSatRecord.FIELD_SPLITTER);
    recordData.append(tmpInRecord.unmapOriginalData());

    tmpOutRecord.setData(recordData.toString());
    Outbatch.add(tmpOutRecord);

    return Outbatch;
  }
}
