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
import OpenRate.record.BalanceImpact;
import OpenRate.record.FlatRecord;
import OpenRate.record.IRecord;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The Output Adapter is responsible for writing the completed records to the
 * target file.
 */
public class BalanceOutputAdapter
        extends FlatFileOutputAdapter {

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public int startTransaction(int TransactionNumber) {

    // We do not have any reason to inhibit the transaction start, so return
    // the OK flag
    return 0;
  }

  @Override
  public Collection<FlatRecord> procValidRecord(IRecord r) {
    FlatRecord tmpOutRecord;
    SeaSatRecord CurrentRecord;
    int i;
    String BalImpactRecord;
    BalanceImpact tmpBalanceImpact;

    Collection<FlatRecord> Outbatch;
    Outbatch = new ArrayList<>();

    CurrentRecord = (SeaSatRecord) r;

    for (i = 0; i < CurrentRecord.getBalanceImpactCount(); i++) {
      tmpBalanceImpact = CurrentRecord.getBalanceImpact(i);
      BalImpactRecord = CurrentRecord.BalanceGroup + ";" + tmpBalanceImpact.counterID + ";" + tmpBalanceImpact.recID + ";" + tmpBalanceImpact.balanceDelta + ";" + tmpBalanceImpact.startDate + ";" + tmpBalanceImpact.endDate;

      tmpOutRecord = new FlatRecord();
      tmpOutRecord.setData(BalImpactRecord);
      Outbatch.add(tmpOutRecord);
    }

    return Outbatch;
  }

  @Override
  public Collection<FlatRecord> procErrorRecord(IRecord r) {
    FlatRecord tmpOutRecord;
    SeaSatRecord CurrentRecord;

    Collection<FlatRecord> Outbatch;
    Outbatch = new ArrayList<>();

    tmpOutRecord = new FlatRecord();
    CurrentRecord = (SeaSatRecord) r;
    tmpOutRecord.setData(CurrentRecord.unmapOriginalData());

    Outbatch.add(tmpOutRecord);

    return Outbatch;
  }
}
