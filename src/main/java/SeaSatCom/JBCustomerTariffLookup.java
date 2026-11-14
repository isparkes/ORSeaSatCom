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
        CurrentRecord.JBUserId = getCustId(CurrentRecord.GuidingKey, CurrentRecord.utcEventDate);
      } catch (Exception ex) {
        CurrentRecord.addError(new RecordError("ERR_JB_CUST_NOT_FOUND", ErrorType.DATA_NOT_FOUND));

        // get out
        return r;
      }

      // *************** Look for service level products/options ***************
      try {
        Products = getProductList(CurrentRecord.GuidingKey, CurrentRecord.ProductService, Subscription, CurrentRecord.utcEventDate);
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
          Products = getProductList(CurrentRecord.GuidingKey, "PACKAGE", Subscription, CurrentRecord.utcEventDate);
        } else if ((CurrentRecord.RECORD_TYPE == SeaSatRecord.SAT_RECORD_TYPE)) {
          Products = getProductList(CurrentRecord.GuidingKey, "SPACKAGE", Subscription, CurrentRecord.utcEventDate);
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
