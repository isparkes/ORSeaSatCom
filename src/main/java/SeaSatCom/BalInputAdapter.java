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

import OpenRate.adapter.file.FlatFileInputAdapter;
import OpenRate.record.IRecord;
import OpenRate.record.FlatRecord;
import OpenRate.record.HeaderRecord;
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
public class BalInputAdapter
  extends FlatFileInputAdapter
{  
  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------

  @Override
  public HeaderRecord procHeader(HeaderRecord r)
  {
    return r;
  }

  @Override
  public IRecord procValidRecord(FlatRecord r)
  {
    return r;
  }

  @Override
  public IRecord procErrorRecord(FlatRecord r)
  {
    return r;
  }

  @Override
  public TrailerRecord procTrailer(TrailerRecord r)
  {
    return r;
  }
}
