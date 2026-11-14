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

import OpenRate.adapter.jdbc.JDBCOutputAdapter;
import OpenRate.record.DBRecord;
import OpenRate.record.FlatRecord;
import OpenRate.record.IRecord;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The BalDBOutput adapter implements the output to the database, based on the
 * JDBCOutputAdapter.
 *
 * @author Ian
 */
public class BalDBOutput extends JDBCOutputAdapter {

  @Override
  public Collection<DBRecord> procValidRecord(IRecord r) {
    String[] Fields;
    DBRecord tmpDataRecord;
    FlatRecord tmpInRecord;

    Collection<DBRecord> Outbatch;
    Outbatch = new ArrayList<>();

      // we have to make a conversion from the flat record to our format
    // which allows the flat file input adapter to do its work without us
    // having to know too much about it.
    tmpInRecord = (FlatRecord) r;
    Fields = tmpInRecord.getData().split(";");
    tmpDataRecord = new DBRecord();
    tmpDataRecord.setOutputColumnCount(6);
    tmpDataRecord.setOutputColumnString(0, Fields[0]);
    tmpDataRecord.setOutputColumnString(1, Fields[1]);
    tmpDataRecord.setOutputColumnString(2, Fields[2]);
    tmpDataRecord.setOutputColumnString(3, Fields[4]);
    tmpDataRecord.setOutputColumnString(4, Fields[5]);
    tmpDataRecord.setOutputColumnString(5, Fields[3]);

    Outbatch.add(tmpDataRecord);

    return Outbatch;
  }


  @Override
  public Collection<DBRecord> procErrorRecord(IRecord r) {
    return null;
  }
}
