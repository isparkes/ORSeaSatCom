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
 * and fitness for a particular purpose are disclaimed. In no event shall
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

import OpenRate.lang.ProductList;
import OpenRate.process.AbstractJBCustomerLookup;
import OpenRate.record.ChargePacket;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;

/**
 * This module sets the customer tariff to a standard value. NOTE that this
 * module is due to be replaced as soon as the jbilling data model is analysed.
 *
 * @author Afzaal
 */
public class JBCustomerTariffLookup extends AbstractJBCustomerLookup {
  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------

  @Override
  public IRecord procValidRecord(IRecord r) {
    SeaSatRecord CurrentRecord = (SeaSatRecord) r;
    ProductList Products;
    String ProdId;
    String Subscription;
    int BaseProductCount = 0;
    int OptionCount = 0;
    ChargePacket tmpCP;

    if ((CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE)
            | (CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE)) {
      //Lookup the customer
      Subscription = CurrentRecord.GuidingKey;
      try {
        CurrentRecord.JBUserId = getCustId(CurrentRecord.GuidingKey, CurrentRecord.UTCEventDate);
      } catch (Exception ex) {
        CurrentRecord.addError(new RecordError("ERR_JB_CUST_NOT_FOUND", ErrorType.DATA_NOT_FOUND));

        // get out
        return r;
      }

      // *************** Look for service level products/options ***************
      try {
        Products = getProductList(CurrentRecord.GuidingKey, CurrentRecord.ProductService, Subscription, CurrentRecord.UTCEventDate);
      } catch (Exception ex) {
        CurrentRecord.addError(new RecordError("ERR_JB_SERVICE_PROD_LOOKUP", ErrorType.DATA_NOT_FOUND));

        // get out
        return r;
      }

      // We now have the product list - get the tariff from it
      if (Products != null) {
        // Get the base products and options
        for (int i = 0; i < Products.getProductCount(); i++) {
          ProdId = Products.getProduct(i).getProductID();

          // is it a base product
          if (ProdId.startsWith("BP:")) {
            BaseProductCount++;

            // error if we have multiple base products
            if (BaseProductCount > 1) {
              CurrentRecord.addError(new RecordError("ERR_JB_MULTIPLE_BASE_PRODUCTS", ErrorType.DATA_NOT_FOUND));
              return r;
            }

            // Set the tariff with what we have found
            CurrentRecord.UserTariff = ProdId.replaceFirst("^BP:", "");
            CurrentRecord.OriginalUserTariff = CurrentRecord.UserTariff;

            // Set the tariff in the CP
            tmpCP = CurrentRecord.getChargePacket(0);
            tmpCP.ratePlanName = CurrentRecord.UserTariff;
          }

          // Is it an option
          if (ProdId.startsWith("OP:")) {
            OptionCount++;
            CurrentRecord.Options.add(ProdId.replaceFirst("^OP:", ""));
          }
        }
      }

      if (BaseProductCount > 0) {
        // we have a service level override - done
        return r;
      }

      // *************** Look for package level products/options ***************
      try {
        if ((CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE)) {
          Products = getProductList(CurrentRecord.GuidingKey, "PACKAGE", Subscription, CurrentRecord.UTCEventDate);
        } else if ((CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE)) {
          Products = getProductList(CurrentRecord.GuidingKey, "SPACKAGE", Subscription, CurrentRecord.UTCEventDate);
        }
      } catch (Exception ex) {
        CurrentRecord.addError(new RecordError("ERR_JB_PACKAGE_PROD_LOOKUP", ErrorType.DATA_NOT_FOUND));

        // get out
        return r;
      }

      // We now have the product list - get the tariff from it
      if (Products != null) {
        for (int i = 0; i < Products.getProductCount(); i++) {
          ProdId = Products.getProduct(i).getProductID();

          // is it a base product
          if (ProdId.startsWith("BP:")) {
            BaseProductCount++;

            // error if we have multiple base products
            if (BaseProductCount > 1) {
              CurrentRecord.addError(new RecordError("ERR_JB_MULTIPLE_BASE_PRODUCTS", ErrorType.DATA_NOT_FOUND));
              return r;
            }

            // Set the tariff with what we have found
            CurrentRecord.UserTariff = ProdId.replaceFirst("^BP:", "");
            CurrentRecord.OriginalUserTariff = CurrentRecord.UserTariff;

            // Set the tariff in the CP
            tmpCP = CurrentRecord.getChargePacket(0);
            tmpCP.ratePlanName = CurrentRecord.UserTariff;
          }

          // Is it an option
          if (ProdId.startsWith("OP:")) {
            OptionCount++;
            CurrentRecord.Options.add(ProdId.replaceFirst("^OP:", ""));
          }
        }
      }

      if (BaseProductCount > 0) {
        // we have a package level product - done
        return r;
      }

      // ************************* Set default products ************************
      if ((CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE)) {
        // for GSM this is allowed
        CurrentRecord.UserTariff = "Default";
        CurrentRecord.OriginalUserTariff = CurrentRecord.UserTariff;

        // Set the tariff in the CP
        tmpCP = CurrentRecord.getChargePacket(0);
        tmpCP.ratePlanName = CurrentRecord.UserTariff;
      }
      if ((CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE)) {
        // for SAT it is not
        CurrentRecord.addError(new RecordError("ERR_JB_NO_BASE_PRODUCT", ErrorType.DATA_NOT_FOUND));
      }
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }
}
