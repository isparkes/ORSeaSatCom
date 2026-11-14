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
  public Collection<FlatRecord> procValidRecord(IRecord r) {
    String ERROR_CODE;
    int RECYCLE_COUNT;
    Collection<FlatRecord> Outbatch;
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
  public Collection<FlatRecord> procErrorRecord(IRecord r) {
    String ERROR_CODE;
    int RECYCLE_COUNT;
    Collection<FlatRecord> Outbatch;
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
