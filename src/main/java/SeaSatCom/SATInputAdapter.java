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

import OpenRate.adapter.file.FlatFileInputAdapter;
import OpenRate.record.FlatRecord;
import OpenRate.record.IRecord;
import OpenRate.record.TrailerRecord;

/**
 * This class is an example of how one would write an InputAdapter. An input
 * adapter implements a single loadBatch() method that selects a set of work and
 * returns it. The framework takes the Collection & pushes it into the first
 * channel where the pipeline begins work on it. Typically the loadBatch()
 * method would read records from a Messaging System, or a DB Table, or a flat
 * file, but in this simplest possible case, it just create a bunch of records
 * out of thin air and passes them on to the pipeline.
 */
public class SATInputAdapter
        extends FlatFileInputAdapter {

  // This is the stream record number counter which tells us the number of
  // the compressed records

  private int StreamRecordNumber;

  // This is the object that is used to compress the records
  SeaSatRecord tmpDataRecord = null;

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public IRecord procHeader(IRecord r) {
    // reset the record numbering
    StreamRecordNumber = 0;

    return r;
  }

  @Override
  public IRecord procValidRecord(IRecord r) {
    String OriginalData;

    // we have to make a conversion from the flat record to our format
    // which allows the flat file input adapter to do its work without us
    // having to know too much about it.
    FlatRecord originalRecord = (FlatRecord) r;
    OriginalData = originalRecord.getData();

    tmpDataRecord = new SeaSatRecord();

    // map the header
    if ((OriginalData.startsWith("\"TERMINAL")) | (OriginalData.startsWith("TERMINAL"))) {
      // header - discard
    } else {
      tmpDataRecord.mapSATRecord(OriginalData);
    }

    // Return the created record
    StreamRecordNumber++;
    tmpDataRecord.RecordNumber = StreamRecordNumber;

    return tmpDataRecord;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {

    // The FlatFileInputAdapter is not able to create error records, so we
    // do not have to do anything for this
    return r;
  }

  @Override
  public IRecord procTrailer(IRecord r) {
    TrailerRecord tmpTrailer;

    // set the trailer record count
    tmpTrailer = (TrailerRecord) r;

    tmpTrailer.setRecordCount(StreamRecordNumber);
    return (IRecord) tmpTrailer;
  }
}
