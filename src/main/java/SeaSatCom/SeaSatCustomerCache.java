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

import OpenRate.OpenRate;
import OpenRate.cache.JBCustomerCache;

/**
 * Provide the parsing for the SeaSatCom customer cache. As lines are read from
 * the customer cache, they are passed into this module to be parsed. We need to
 * extract the information out of the line we are passed to give us:
 *  - the product to use
 *  - the service (e.g. Telephony) to use
 *  - the "alias" (the identifier that tells which customer is involved)
 *  - the subscription to use
 * 
 * Note that we write log messages to the OpenRate framework log, because that
 * is where other resource classes write their messages. (Resources can be used
 * by any pipeline, so it makes little sense to write messages to the pipe log).
 */
public class SeaSatCustomerCache extends JBCustomerCache
{
  
 /**
  * Get the product name from the description string
  *
  * @param DescriptionString Description string to parse
  * @return The product name
  */
  @Override
  public String getProduct(String DescriptionString)
  {
    String[] SplitParts1;
    String[] SplitParts2;
    String   ProdName;

    // split the rest of the description
    SplitParts1 = DescriptionString.split("/");
    SplitParts2 = SplitParts1[1].split(":");

    ProdName = SplitParts1[0].trim().replaceAll("\\[.*\\]", "").trim() + ":" + SplitParts2[1].trim();

    return ProdName;
  }

 /**
  * Get the product name from the description string
  *
  * @param DescriptionString Description string to parse
  * @return The product name
  */
  @Override
  public String getService(String DescriptionString)
  {
    String[] SplitParts1;
    String[] SplitParts2;
    String   Service;

    // split the rest of the description
    SplitParts1 = DescriptionString.split("/");
    SplitParts2 = SplitParts1[1].split(":");

    Service = SplitParts2[0].trim();

    return Service;
  }

 /**
  * Get the login from the description string
  *
  * @param DescriptionString Description string to parse
  * @return The login
  */
  @Override
  public String getAlias(String DescriptionString)
  {
    String[] SplitParts1;
    String   Login = "";

    // only get the
    if (DescriptionString.contains(" OP /"))
    {
      SplitParts1 = DescriptionString.split("OP");
    }
    else if (DescriptionString.contains(" BP /"))
    {
      SplitParts1 = DescriptionString.split("BP");
    }
    else
    {
      return "";
    }
    
    if (SplitParts1.length != 2)
    {
      OpenRate.getOpenRateFrameworkLog().error("Login not found in product info <" + DescriptionString + ">");
    }
    else
    {
      Login = SplitParts1[0].replaceAll("\\[", "").replaceAll("\\]", "").trim();
    }

    return Login;
  }

 /**
  * Get the subscription from the description string. Uses the login as default.
  *
  * @param DescriptionString Description string to parse
  * @return The login
  */
  @Override
  public String getSubscription(String DescriptionString)
  {
    return getAlias(DescriptionString);
  }
}
