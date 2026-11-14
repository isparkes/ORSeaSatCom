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

import OpenRate.process.AbstractRegexMatch;
import OpenRate.record.IRecord;

/**
 * This class looks up if any options in use override the base rate plan. In the
 * case that they do, the rate plan is altered.
 */
public class OptionOverride
  extends AbstractRegexMatch
{
  // String array used for searching
  private final String[] tmpSearchParameters = new String[3];

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------

  @Override
  public IRecord procValidRecord(IRecord r)
  {
    String RegexGroup;
    String RegexResult;
    int Index;
    
    SeaSatRecord CurrentRecord = (SeaSatRecord) r;

    if ((CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE) |
        (CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE))
    {
      for (Index = 0 ; Index < CurrentRecord.Options.size() ; Index++)
      {
        // the group is the rate sheet
        RegexGroup = CurrentRecord.PriceSheet;

        // Prepare the paramters to perform the search on
        tmpSearchParameters[0] = CurrentRecord.Options.get(Index);
        tmpSearchParameters[1] = CurrentRecord.OriginZone;
        tmpSearchParameters[2] = CurrentRecord.DestinationZone;
        
        // Look up the rate model to use
        RegexResult = getRegexMatch(RegexGroup,tmpSearchParameters);
        if (RegexResult.equals("NOMATCH"))
        {
          // don't need to do anything, this option has no effect
        }
        else
        {
          CurrentRecord.UserTariff = CurrentRecord.Options.get(Index);
        }
      }
    }
    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r)
  {
    return r;
  }
}

