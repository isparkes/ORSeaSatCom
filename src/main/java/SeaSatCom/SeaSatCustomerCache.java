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
