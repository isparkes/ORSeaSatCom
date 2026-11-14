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
 * The Output Adapter is responsible for writing correctly rated records to the
 * file for loading into jBilling.
 *
 * @author afzaal
 */
public class JBillingOutput extends FlatFileOutputAdapter {

  @Override
  public Collection<FlatRecord> procValidRecord(IRecord r) {

    Collection<FlatRecord> Outbatch;
    Outbatch = new ArrayList<>();
    FlatRecord tmpOutRecord = new FlatRecord();
    SeaSatRecord tmpInRecord = (SeaSatRecord) r;

    if (tmpInRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE) {
      tmpOutRecord.setData(tmpInRecord.unmapJBillingGSMData());
      Outbatch.add(tmpOutRecord);

      // deal with the pack usage if we need to
      if (tmpInRecord.PackUsageFlag == 1) {
        // we have pack usage, write the output line for it
        FlatRecord tmpPackOutRecord = new FlatRecord();
        tmpPackOutRecord.setData(tmpInRecord.unmapJBillingPackData());
        Outbatch.add(tmpPackOutRecord);
      }
      return Outbatch;
    } else if (tmpInRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE) {
      tmpOutRecord.setData(tmpInRecord.unmapJBillingSATData());
      Outbatch.add(tmpOutRecord);

      // deal with the pack usage if we need to
      if (tmpInRecord.PackUsageFlag == 1) {
        // we have pack usage, write the output line for it
        FlatRecord tmpPackOutRecord = new FlatRecord();
        tmpPackOutRecord.setData(tmpInRecord.unmapJBillingPackData());
        Outbatch.add(tmpPackOutRecord);
      }
      return Outbatch;
    } else {
      return null;
    }
  }

  @Override
  public Collection<FlatRecord> procErrorRecord(IRecord r) {
    return null;
  }
}
