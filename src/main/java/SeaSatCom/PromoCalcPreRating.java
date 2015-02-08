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

import OpenRate.exception.InitializationException;
import OpenRate.lang.DiscountInformation;
import OpenRate.process.AbstractBalanceHandlerPlugIn;
import OpenRate.record.ErrorType;
import OpenRate.record.IRecord;
import OpenRate.record.RecordError;
import OpenRate.utils.ConversionUtils;
import OpenRate.utils.PropertyUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Now that we have the prioritised list of products and promotions, we ca work
 * out the consuming of the balances that there might be, before we pass into
 * rating the values of what is left after consumption. This will decrement
 * balances, passing the results on for rating.
 */
public class PromoCalcPreRating extends AbstractBalanceHandlerPlugIn {

  // Used for calculating the validites of the counters

  private final ConversionUtils conversionUtils = new ConversionUtils();

  // this is the control item for the rating and promotional work
  private class RatingControlItem {

    String productID;
    int counterId;
    double threshold;
    String Period;
  }

  // These allows the lookup of the promotional steps to perform for each promo
  private HashMap<String, RatingControlItem> RatingControlDur;
  private HashMap<String, RatingControlItem> RatingControlEvt;
  private HashMap<String, RatingControlItem> RatingControlVol;

  // -----------------------------------------------------------------------------
  // ------------------ Start of initialisation functions ------------------------
  // -----------------------------------------------------------------------------
  @Override
  public void init(String PipelineName, String ModuleName)
          throws InitializationException {
    String BalanceConfigFile;

    // do the inherited initialisation
    super.init(PipelineName, ModuleName);

    // Initialise the Rating Control Objects
    RatingControlDur = new HashMap<>(50);
    RatingControlEvt = new HashMap<>(50);
    RatingControlVol = new HashMap<>(50);

    BalanceConfigFile = PropertyUtils.getPropertyUtils().getPluginPropertyValue(PipelineName,
            ModuleName,
            "BalanceConfig");

    if (BalanceConfigFile == null) {
      getPipeLog().fatal("Could not find config file for <" + getSymbolicName() + ">");
      throw new InitializationException("Could not find config file for <"
              + getSymbolicName() + ">", getSymbolicName());
    }

    // Load the configuration
    loadConfigFromFile(BalanceConfigFile);
  }

  // -----------------------------------------------------------------------------
  // ------------------ Start of inherited Plug In functions ---------------------
  // -----------------------------------------------------------------------------
  @Override
  public IRecord procValidRecord(IRecord r) {
    RatingControlItem tmpRatingControlItem;
    SeaSatRecord CurrentRecord;
    DiscountInformation tmpDiscInfo;

    boolean BundleFound = false;

    CurrentRecord = (SeaSatRecord) r;

    // Used for balance creation
    long tmpStartDate = 0;
    long tmpEndDate = 0;
    RecordError tmpError;

    if (CurrentRecord.RECORD_TYPE == SeaSatRecord.GSM_RECORD_TYPE) {
      if (CurrentRecord.UsedDiscount.trim().isEmpty()) {
        // no work to do - go home
        return r;
      }

      // ******************************** DUR **********************************
      try {
        // Get the bundle metadata for this product
        if (RatingControlDur.containsKey(CurrentRecord.UsedDiscount)) {
          // Mark that we have found it
          BundleFound = true;

          if (CurrentRecord.getRUMValue("DUR") > 0) {
            // Get the control item
            tmpRatingControlItem = RatingControlDur.get(CurrentRecord.UsedDiscount);

            // Create the daily or monthly balance
            if (tmpRatingControlItem.Period.equalsIgnoreCase("m")) {
              tmpStartDate = conversionUtils.getUTCDayStart(CurrentRecord.BillingCycleStart);
              tmpEndDate = conversionUtils.getUTCDayEnd(CurrentRecord.BillingCycleEnd);
            } else if (tmpRatingControlItem.Period.equalsIgnoreCase("d")) {
              tmpStartDate = conversionUtils.getUTCDayStart(CurrentRecord.EventStartDate);
              tmpEndDate = conversionUtils.getUTCDayEnd(CurrentRecord.EventStartDate);
            }

            // perform the discount
            tmpDiscInfo = discountConsumeRUM(CurrentRecord, CurrentRecord.UsedDiscount, CurrentRecord.BalanceGroup, "DUR", tmpRatingControlItem.counterId, tmpRatingControlItem.threshold, tmpStartDate, tmpEndDate);

            // put the info back in the Record
            if (tmpDiscInfo.isDiscountApplied()) {
              CurrentRecord.DiscountRUM = "DUR";
              CurrentRecord.DiscountRule = "ConsumeDUR";
              CurrentRecord.DiscountGranted = tmpDiscInfo.getDiscountedValue();
              CurrentRecord.RecId = tmpDiscInfo.getRecId();
              CurrentRecord.CounterCycle = tmpDiscInfo.getCounterId();
            }
          }
        }
      } catch (Exception e) {
        CurrentRecord.addError(new RecordError("ERR_DURATION_DISOUNT_FAIL", ErrorType.SPECIAL));
      }

      // ******************************** EVT **********************************
      try {
        // Get the bundle metadata for this product
        if (RatingControlEvt.containsKey(CurrentRecord.UsedDiscount)) {
          // Mark that we have found it
          BundleFound = true;

          if (CurrentRecord.getRUMValue("EVT") > 0) {
            // Get the control item
            tmpRatingControlItem = RatingControlEvt.get(CurrentRecord.UsedDiscount);

            // Create the daily or monthly balance
            if (tmpRatingControlItem.Period.equalsIgnoreCase("m")) {
              tmpStartDate = conversionUtils.getUTCDayStart(CurrentRecord.BillingCycleStart);
              tmpEndDate = conversionUtils.getUTCDayEnd(CurrentRecord.BillingCycleEnd);
            } else if (tmpRatingControlItem.Period.equalsIgnoreCase("d")) {
              tmpStartDate = conversionUtils.getUTCDayStart(CurrentRecord.EventStartDate);
              tmpEndDate = conversionUtils.getUTCDayEnd(CurrentRecord.EventStartDate);
            }

            // perform the discount
            tmpDiscInfo = discountConsumeRUM(CurrentRecord, CurrentRecord.UsedDiscount, CurrentRecord.BalanceGroup, "EVT", tmpRatingControlItem.counterId, tmpRatingControlItem.threshold, tmpStartDate, tmpEndDate);

            // put the info back in the Record
            if (tmpDiscInfo.isDiscountApplied()) {
              CurrentRecord.DiscountRUM = "EVT";
              CurrentRecord.DiscountRule = "ConsumeEVT";
              CurrentRecord.DiscountGranted = tmpDiscInfo.getDiscountedValue();
              CurrentRecord.RecId = tmpDiscInfo.getRecId();
              CurrentRecord.CounterCycle = tmpDiscInfo.getCounterId();
            }
          }
        }
      } catch (Exception e) {
        CurrentRecord.addError(new RecordError("ERR_EVENT_DISOUNT_FAIL", ErrorType.SPECIAL));
      }

      // ******************************** VOL **********************************
      try {
        // Get the bundle metadata for this product
        if (RatingControlVol.containsKey(CurrentRecord.UsedDiscount)) {
          // Mark that we have found it
          BundleFound = true;

          if (CurrentRecord.getRUMValue("VOL") > 0) {
            // Get the control item
            tmpRatingControlItem = RatingControlVol.get(CurrentRecord.UsedDiscount);

            // Create the daily or monthly balance
            if (tmpRatingControlItem.Period.equalsIgnoreCase("m")) {
              tmpStartDate = conversionUtils.getUTCDayStart(CurrentRecord.BillingCycleStart);
              tmpEndDate = conversionUtils.getUTCDayEnd(CurrentRecord.BillingCycleEnd);
            } else if (tmpRatingControlItem.Period.equalsIgnoreCase("d")) {
              tmpStartDate = conversionUtils.getUTCDayStart(CurrentRecord.EventStartDate);
              tmpEndDate = conversionUtils.getUTCDayEnd(CurrentRecord.EventStartDate);
            }

            // perform the discount
            tmpDiscInfo = discountConsumeRUM(CurrentRecord, CurrentRecord.UsedDiscount, CurrentRecord.BalanceGroup, "VOL", tmpRatingControlItem.counterId, tmpRatingControlItem.threshold, tmpStartDate, tmpEndDate);

            // put the info back in the Record
            if (tmpDiscInfo.isDiscountApplied()) {
              CurrentRecord.DiscountRUM = "VOL";
              CurrentRecord.DiscountRule = "ConsumeVOL";
              CurrentRecord.DiscountGranted = tmpDiscInfo.getDiscountedValue();
              CurrentRecord.RecId = tmpDiscInfo.getRecId();
              CurrentRecord.CounterCycle = tmpDiscInfo.getCounterId();
            }

            if (tmpDiscInfo.isBalanceCreated()) {
              // Set the pack usage flag, for daily counters only
              if (tmpRatingControlItem.Period.equalsIgnoreCase("d")) {
                CurrentRecord.PackUsageFlag = 1;
                CurrentRecord.setRUMValue("PACK", 1);
              }
            }
          }
        }
      } catch (Exception e) {
        CurrentRecord.addError(new RecordError("ERR_VOLUME_DISOUNT_FAIL", ErrorType.SPECIAL));
      }

      // deal with friends and family
      if (CurrentRecord.UsedDiscount.equals("Numero_Absolus")) {
        BundleFound = true;
      }

      // See if we missed the bundle
      if (BundleFound == false) {
        // bundle not defined - error
        tmpError = new RecordError("ERR_BUNDLE_NOT_DEFINED", ErrorType.SPECIAL);
        CurrentRecord.addError(tmpError);
      }
    }

    return r;
  }

  @Override
  public IRecord procErrorRecord(IRecord r) {
    return r;
  }

  /**
   * Load the data from the defined file
   *
   * @param BalanceConfigFile Name of the file that balances control information
   * is loaded from
   * @throws OpenRate.exception.InitializationException
   */
  public void loadConfigFromFile(String BalanceConfigFile)
          throws InitializationException {
    // Variable declarations
    int BalsLoaded = 0;
    BufferedReader inFile;
    String tmpFileRecord;
    String[] BalFields;

    // Loading fields
    double Threshold;
    String tmpType;
    String tmpThreshold;
    String tmpProduct;
    String tmpCounterId;
    int CounterId;
    String tmpPeriod;

    // Log that we are starting the loading
    getPipeLog().info("Starting Balance Configuration Loading from File");

    // Try to open the file
    try {
      inFile = new BufferedReader(new FileReader(BalanceConfigFile));
    } catch (FileNotFoundException exFileNotFound) {
      getPipeLog().error(
              "Application is not able to read file : <"
              + BalanceConfigFile + ">");
      throw new InitializationException("Application is not able to read file: <"
              + BalanceConfigFile + ">",
              exFileNotFound, getSymbolicName());
    }

    // File open, now get the stuff
    try {
      while (inFile.ready()) {
        tmpFileRecord = inFile.readLine();

        if ((tmpFileRecord.startsWith("#"))
                | tmpFileRecord.trim().equals("")) {
          // Comment line, ignore
        } else {
          BalFields = tmpFileRecord.split(";");

          tmpType = BalFields[0];
          tmpProduct = BalFields[1];
          tmpCounterId = BalFields[2];
          tmpThreshold = BalFields[3];
          tmpPeriod = BalFields[4];
          CounterId = Integer.parseInt(tmpCounterId);
          Threshold = Double.parseDouble(tmpThreshold);

          if (tmpType.equalsIgnoreCase("dur")) {
            addConfigDur(tmpProduct, CounterId, Threshold, tmpPeriod);
            BalsLoaded++;
          }

          if (tmpType.equalsIgnoreCase("evt")) {
            addConfigEvt(tmpProduct, CounterId, Threshold, tmpPeriod);
            BalsLoaded++;
          }

          if (tmpType.equalsIgnoreCase("vol")) {
            addConfigVol(tmpProduct, CounterId, Threshold, tmpPeriod);
            BalsLoaded++;
          }
        }
      }
    } catch (IOException ex) {
      getPipeLog().fatal(
              "Error reading input file <" + BalanceConfigFile
              + "> in record <" + BalsLoaded + ">. IO Error.");
    } catch (ArrayIndexOutOfBoundsException ex) {
      getPipeLog().fatal(
              "Error reading input file <" + BalanceConfigFile
              + "> in record <" + BalsLoaded + ">. Malformed Record.");
    } finally {
      try {
        inFile.close();
      } catch (IOException ex) {
        getPipeLog().error("Error closing input file <" + BalanceConfigFile
                + ">", ex);
      }
    }

    getPipeLog().info(
            "Balance Configuration Cache Data Loading completed. " + BalsLoaded
            + " configuration lines loaded from <" + BalanceConfigFile
            + ">");
  }

  /**
   * Add a new configuration into the list for the DUR discounts
   */
  private void addConfigDur(String tmpProduct, int CounterId, double Threshold, String Period) {
    RatingControlItem tmpRatingControlItem;

    tmpRatingControlItem = new RatingControlItem();
    tmpRatingControlItem.productID = tmpProduct;
    tmpRatingControlItem.counterId = CounterId;
    tmpRatingControlItem.threshold = Threshold;
    tmpRatingControlItem.Period = Period;
    RatingControlDur.put(tmpProduct, tmpRatingControlItem);
  }

  /**
   * Add a new configuration into the list for the EVT discounts
   */
  private void addConfigEvt(String tmpProduct, int CounterId, double Threshold, String Period) {
    RatingControlItem tmpRatingControlItem;

    tmpRatingControlItem = new RatingControlItem();
    tmpRatingControlItem.productID = tmpProduct;
    tmpRatingControlItem.counterId = CounterId;
    tmpRatingControlItem.threshold = Threshold;
    tmpRatingControlItem.Period = Period;
    RatingControlEvt.put(tmpProduct, tmpRatingControlItem);
  }

  /**
   * Add a new configuration into the list for the EVT discounts
   */
  private void addConfigVol(String tmpProduct, int CounterId, double Threshold, String Period) {
    RatingControlItem tmpRatingControlItem;

    tmpRatingControlItem = new RatingControlItem();
    tmpRatingControlItem.productID = tmpProduct;
    tmpRatingControlItem.counterId = CounterId;
    tmpRatingControlItem.threshold = Threshold;
    tmpRatingControlItem.Period = Period;
    RatingControlVol.put(tmpProduct, tmpRatingControlItem);
  }
}
