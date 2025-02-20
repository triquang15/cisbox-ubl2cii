package com.cisbox.common;

import java.text.*;
import java.time.*;
import java.util.*;

import javax.annotation.*;

import com.helger.commons.*;
import com.helger.commons.datetime.*;
import com.helger.commons.error.list.*;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.*;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.*;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.*;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.*;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.AmountType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.QuantityType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.TextType;

/**
 * UBL 2.1 to CII D16B converter.
 *
 * @author Vartika Rastogi
 * @author Philip Helger
 */
public final class UBL21ToCII16BConverter
{
  private static final String CII_DATE_FORMAT = "102";

  private UBL21ToCII16BConverter ()
  {}

  @Nonnull
  private static String _createFormattedDateValue (@Nonnull final LocalDate aLocalDate)
  {
    final SimpleDateFormat aFormatter = new SimpleDateFormat ("yyyyMMdd");
    final Date aDate = PDTFactory.createDate (aLocalDate);
    return aFormatter.format (aDate);
  }

  @Nonnull
  private static un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType _createDate (@Nonnull final LocalDate aLocalDate)
  {
    final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType aDTT = new un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType ();
    {
      final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString aDTS = new un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString ();
      aDTS.setFormat (CII_DATE_FORMAT);
      aDTS.setValue (_createFormattedDateValue (aLocalDate));
      aDTT.setDateTimeString (aDTS);
    }
    return aDTT;
  }

  @Nonnull
  private static List <TextType> _convertTextType (@Nonnull final String sValue)
  {
    final List <TextType> aLstTT = new ArrayList <> ();
    final TextType aTT = new TextType ();
    aTT.setValue (sValue);
    aLstTT.add (aTT);
    return aLstTT;
  }

  @Nonnull
  private static IDType _convertIDType (@Nonnull final com.helger.xsds.ccts.cct.schemamodule.IdentifierType aID)
  {
    final IDType aITG = new IDType ();
    aITG.setSchemeID (aID.getSchemeID ());
    aITG.setValue (aID.getValue ());
    return aITG;
  }

  @Nonnull
  private static List <AmountType> _convertAmountType (final com.helger.xsds.ccts.cct.schemamodule.AmountType aUBLAmount)
  {
    final List <AmountType> aLstATTPT = new ArrayList <> ();
    final AmountType aATTPT = new AmountType ();
    if (false)
      aATTPT.setCurrencyID (aUBLAmount.getCurrencyID ());
    aATTPT.setValue (aUBLAmount.getValue ());
    aLstATTPT.add (aATTPT);
    return aLstATTPT;
  }

  @Nonnull
  private static List <un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType> _convertIncludedNote (final NoteType aNote)
  {
    final List <un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType> lstILTNT = new ArrayList <> ();
    final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType aNTSC = new un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType ();
    {
      final TextType aTT = new TextType ();
      aTT.setValue (aNote.getValue ());
      aNTSC.addContent (aTT);
    }
    lstILTNT.add (aNTSC);
    return lstILTNT;
  }

  @Nonnull
  private static List <SupplyChainTradeLineItemType> _convertInvoiceLine (final List <oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType> aLstIL)
  {
    final List <SupplyChainTradeLineItemType> aLstSCTLIT = new ArrayList <> ();
    for (final InvoiceLineType aILT : aLstIL)
    {
      final SupplyChainTradeLineItemType aISCTLI = new SupplyChainTradeLineItemType ();
      final DocumentLineDocumentType aDLDT = new DocumentLineDocumentType ();

      aDLDT.setLineID (aILT.getIDValue ());

      if (!aILT.getNote ().isEmpty ())
      {
        aDLDT.setIncludedNote (_convertIncludedNote (aILT.getNote ().get (0)));
      }

      aISCTLI.setAssociatedDocumentLineDocument (aDLDT);

      // SpecifiedTradeProduct
      final TradeProductType aTPT = new TradeProductType ();
      final ItemType aIT = aILT.getItem ();
      if (aIT.getStandardItemIdentification () != null)
      {
        aTPT.setGlobalID (_convertIDType (aIT.getStandardItemIdentification ().getID ()));
      }

      if (aIT.getSellersItemIdentification () != null)
      {
        aTPT.setSellerAssignedID (aIT.getSellersItemIdentification ().getIDValue ());
      }

      aTPT.setName (_convertTextType (aIT.getNameValue ()));

      if (!aIT.getDescription ().isEmpty ())
      {
        aTPT.setDescription (aIT.getDescription ().get (0).getValue ());
      }

      // ApplicableProductCharacteristic
      final List <ProductCharacteristicType> aLstPCT = new ArrayList <> ();
      for (final ItemPropertyType aIPT : aILT.getItem ().getAdditionalItemProperty ())
      {
        final ProductCharacteristicType aPCT = new ProductCharacteristicType ();
        aPCT.setDescription (_convertTextType (aIPT.getNameValue ()));
        aPCT.setValue (_convertTextType (aIPT.getValueValue ()));
        aLstPCT.add (aPCT);
      }
      aTPT.setApplicableProductCharacteristic (aLstPCT);

      // DesignatedProductClassification
      final List <ProductClassificationType> aLstPCCT = new ArrayList <> ();
      for (final CommodityClassificationType aCCT : aILT.getItem ().getCommodityClassification ())
      {
        final ProductClassificationType aPCT = new ProductClassificationType ();
        final CodeType aCT = new CodeType ();
        aCT.setListID (aCCT.getItemClassificationCode ().getListID ());
        aCT.setValue (aCCT.getItemClassificationCode ().getValue ());
        aPCT.setClassCode (aCT);
        aLstPCCT.add (aPCT);
      }
      aTPT.setDesignatedProductClassification (aLstPCCT);
      aISCTLI.setSpecifiedTradeProduct (aTPT);

      // SpecifiedLineTradeAgreement
      final LineTradeAgreementType aLTAT = new LineTradeAgreementType ();
      // BuyerOrderReferencedDocument
      final ReferencedDocumentType aRDT = new ReferencedDocumentType ();
      if (!aILT.getOrderLineReference ().isEmpty ())
      {
        aRDT.setLineID (aILT.getOrderLineReference ().get (0).getLineIDValue ());
      }

      // NetPriceProductTradePrice
      final TradePriceType aLTPT = new TradePriceType ();
      if (aILT.getPrice () != null && aILT.getPrice ().getPriceAmount () != null)
      {
        aLTPT.setChargeAmount (_convertAmountType (aILT.getPrice ().getPriceAmount ()));
      }

      aLTAT.setBuyerOrderReferencedDocument (aRDT);
      aLTAT.setNetPriceProductTradePrice (aLTPT);
      aISCTLI.setSpecifiedLineTradeAgreement (aLTAT);

      // SpecifiedLineTradeDelivery
      final LineTradeDeliveryType aLTDT = new LineTradeDeliveryType ();
      final QuantityType aQT = new QuantityType ();
      aQT.setUnitCode (aILT.getInvoicedQuantity ().getUnitCode ());
      aQT.setValue (aILT.getInvoicedQuantity ().getValue ());
      aLTDT.setBilledQuantity (aQT);
      aISCTLI.setSpecifiedLineTradeDelivery (aLTDT);

      // SpecifiedLineTradeSettlement
      final LineTradeSettlementType aLTST = new LineTradeSettlementType ();
      {
        final List <TradeTaxType> aLstTTT = new ArrayList <> ();
        for (final TaxCategoryType aTCT : aILT.getItem ().getClassifiedTaxCategory ())
        {
          final TradeTaxType aTTT = new TradeTaxType ();
          aTTT.setTypeCode (aTCT.getTaxScheme ().getIDValue ());
          aTTT.setCategoryCode (aTCT.getIDValue ());
          if (aTCT.getPercent () != null)
            aTTT.setRateApplicablePercent (aTCT.getPercentValue ());
          aLstTTT.add (aTTT);
        }
        aLTST.setApplicableTradeTax (aLstTTT);
      }
      final TradeSettlementLineMonetarySummationType aTSLMST = new TradeSettlementLineMonetarySummationType ();
      if (aILT.getLineExtensionAmount () != null)
      {
        aTSLMST.setLineTotalAmount (_convertAmountType (aILT.getLineExtensionAmount ()));
      }

      if (aILT.getAccountingCostValue () != null)
      {
        final List <TradeAccountingAccountType> aLstTAATL = new ArrayList <> ();
        final TradeAccountingAccountType aTAATL = new TradeAccountingAccountType ();
        aTAATL.setID (aILT.getAccountingCostValue ());
        aLstTAATL.add (aTAATL);
        aLTST.setReceivableSpecifiedTradeAccountingAccount (aLstTAATL);
      }

      aLTST.setSpecifiedTradeSettlementLineMonetarySummation (aTSLMST);
      aISCTLI.setSpecifiedLineTradeSettlement (aLTST);

      aLstSCTLIT.add (aISCTLI);
    }
    return aLstSCTLIT;
  }

  @Nonnull
  private static TradePartyType _convertSellerTradeParty (final InvoiceType aUBLInvoice)
  {
    final TradePartyType aTPT = new TradePartyType ();
    final List <IDType> aLstIT = new ArrayList <> ();

    if (aUBLInvoice.getAccountingSupplierParty () != null &&
        aUBLInvoice.getAccountingSupplierParty ().getParty () != null &&
        !aUBLInvoice.getAccountingSupplierParty ().getParty ().getPartyIdentification ().isEmpty () &&
        aUBLInvoice.getAccountingSupplierParty ().getParty ().getPartyIdentification ().get (0).getID () != null)
    {
      aLstIT.add (_convertIDType (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPartyIdentification ().get (0).getID ()));
      aTPT.setID (aLstIT);
    }

    if (aUBLInvoice.getAccountingSupplierParty () != null &&
        aUBLInvoice.getAccountingSupplierParty ().getParty () != null &&
        !aUBLInvoice.getAccountingSupplierParty ().getParty ().getPartyName ().isEmpty ())
    {
      aTPT.setName (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPartyName ().get (0).getNameValue ());
    }

    if (aUBLInvoice.getAccountingSupplierParty () != null &&
        aUBLInvoice.getAccountingSupplierParty ().getParty () != null &&
        !aUBLInvoice.getAccountingSupplierParty ().getParty ().getPartyLegalEntity ().isEmpty ())
    {
      final LegalOrganizationType aLOT = new LegalOrganizationType ();

      aLOT.setTradingBusinessName (aUBLInvoice.getAccountingSupplierParty ()
                                              .getParty ()
                                              .getPartyLegalEntity ()
                                              .get (0)
                                              .getRegistrationNameValue ());
      if (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPartyLegalEntity ().get (0).getCompanyID () != null)
      {
        aLOT.setID (_convertIDType (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPartyLegalEntity ().get (0).getCompanyID ()));
      }

      final TradeAddressType aTAT = new TradeAddressType ();
      if (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPartyLegalEntity ().get (0).getRegistrationAddress () != null)
      {
        if (aUBLInvoice.getAccountingSupplierParty ()
                       .getParty ()
                       .getPartyLegalEntity ()
                       .get (0)
                       .getRegistrationAddress ()
                       .getCityName () != null)
        {
          aTAT.setCityName (aUBLInvoice.getAccountingSupplierParty ()
                                       .getParty ()
                                       .getPartyLegalEntity ()
                                       .get (0)
                                       .getRegistrationAddress ()
                                       .getCityName ()
                                       .getValue ());
        }
        if (aUBLInvoice.getAccountingSupplierParty ()
                       .getParty ()
                       .getPartyLegalEntity ()
                       .get (0)
                       .getRegistrationAddress ()
                       .getCountry () != null &&
            aUBLInvoice.getAccountingSupplierParty ()
                       .getParty ()
                       .getPartyLegalEntity ()
                       .get (0)
                       .getRegistrationAddress ()
                       .getCountry ()
                       .getIdentificationCode () != null)
        {
          aTAT.setCountryID (aUBLInvoice.getAccountingSupplierParty ()
                                        .getParty ()
                                        .getPartyLegalEntity ()
                                        .get (0)
                                        .getRegistrationAddress ()
                                        .getCountry ()
                                        .getIdentificationCode ()
                                        .getValue ());
        }
        if (aUBLInvoice.getAccountingSupplierParty ()
                       .getParty ()
                       .getPartyLegalEntity ()
                       .get (0)
                       .getRegistrationAddress ()
                       .getCountrySubentity () != null)
        {
          aTAT.setCountrySubDivisionName (_convertTextType (aUBLInvoice.getAccountingSupplierParty ()
                                                                       .getParty ()
                                                                       .getPartyLegalEntity ()
                                                                       .get (0)
                                                                       .getRegistrationAddress ()
                                                                       .getCountrySubentity ()
                                                                       .getValue ()));
        }
      }
      aLOT.setPostalTradeAddress (aTAT);

      aTPT.setSpecifiedLegalOrganization (aLOT);
    }

    if (aUBLInvoice.getAccountingSupplierParty () != null &&
        aUBLInvoice.getAccountingSupplierParty ().getParty () != null &&
        aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress () != null)
    {
      final TradeAddressType aTATR = new TradeAddressType ();
      if (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress ().getPostalZone () != null)
      {
        aTATR.setPostcodeCode (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress ().getPostalZone ().getValue ());
      }
      if (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress ().getStreetName () != null)
      {
        aTATR.setLineOne (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress ().getStreetName ().getValue ());
      }
      if (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress ().getAdditionalStreetName () != null)
      {
        aTATR.setLineTwo (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress ().getAdditionalStreetName ().getValue ());
      }
      if (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress ().getCityName () != null)
      {
        aTATR.setCityName (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress ().getCityName ().getValue ());
      }
      if (aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress ().getCountry () != null &&
          aUBLInvoice.getAccountingSupplierParty ().getParty ().getPostalAddress ().getCountry ().getIdentificationCode () != null)
      {
        aTATR.setCountryID (aUBLInvoice.getAccountingSupplierParty ()
                                       .getParty ()
                                       .getPostalAddress ()
                                       .getCountry ()
                                       .getIdentificationCode ()
                                       .getValue ());
      }
      aTPT.setPostalTradeAddress (aTATR);
    }

    if (aUBLInvoice.getAccountingSupplierParty () != null &&
        aUBLInvoice.getAccountingSupplierParty ().getParty () != null &&
        aUBLInvoice.getAccountingSupplierParty ().getParty ().getEndpointID () != null)
    {
      final List <UniversalCommunicationType> aLstUCT = new ArrayList <> ();
      final UniversalCommunicationType aUCT = new UniversalCommunicationType ();
      aUCT.setURIID (_convertIDType (aUBLInvoice.getAccountingSupplierParty ().getParty ().getEndpointID ()));
      aLstUCT.add (aUCT);
      aTPT.setURIUniversalCommunication (aLstUCT);
    }

    if (aUBLInvoice.getAccountingSupplierParty () != null &&
        aUBLInvoice.getAccountingSupplierParty ().getParty () != null &&
        !aUBLInvoice.getAccountingSupplierParty ().getParty ().getPartyTaxScheme ().isEmpty ())
    {
      final List <TaxRegistrationType> aLstTRT = new ArrayList <> ();
      final TaxRegistrationType aTRT = new TaxRegistrationType ();
      aTRT.setID (_convertIDType (aUBLInvoice.getAccountingSupplierParty ()
                                             .getParty ()
                                             .getPartyTaxScheme ()
                                             .get (0)
                                             .getTaxScheme ()
                                             .getID ()));
      aLstTRT.add (aTRT);
      aTPT.setSpecifiedTaxRegistration (aLstTRT);
    }
    return aTPT;
  }

  @Nonnull
  private static TradePartyType _convertBuyerTradeParty (final InvoiceType aUBLInvoice)
  {
    final TradePartyType aBTPT = new TradePartyType ();
    if (aUBLInvoice.getAccountingCustomerParty () != null &&
        aUBLInvoice.getAccountingCustomerParty ().getParty () != null &&
        !aUBLInvoice.getAccountingCustomerParty ().getParty ().getPartyIdentification ().isEmpty ())
    {
      final List <IDType> aLstBIT = new ArrayList <> ();
      aLstBIT.add (_convertIDType (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPartyIdentification ().get (0).getID ()));
      aBTPT.setID (aLstBIT);
    }

    if (aUBLInvoice.getAccountingCustomerParty () != null &&
        aUBLInvoice.getAccountingCustomerParty ().getParty () != null &&
        !aUBLInvoice.getAccountingCustomerParty ().getParty ().getPartyName ().isEmpty ())
    {
      aBTPT.setName (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPartyName ().get (0).getNameValue ());
    }

    final LegalOrganizationType aBLOT = new LegalOrganizationType ();
    if (aUBLInvoice.getAccountingCustomerParty () != null &&
        aUBLInvoice.getAccountingCustomerParty ().getParty () != null &&
        !aUBLInvoice.getAccountingCustomerParty ().getParty ().getPartyLegalEntity ().isEmpty ())
    {
      aBLOT.setTradingBusinessName (aUBLInvoice.getAccountingCustomerParty ()
                                               .getParty ()
                                               .getPartyLegalEntity ()
                                               .get (0)
                                               .getRegistrationNameValue ());
      if (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPartyLegalEntity ().get (0).getCompanyID () != null)
      {
        aBLOT.setID (_convertIDType (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPartyLegalEntity ().get (0).getCompanyID ()));
      }
    }

    if (aUBLInvoice.getAccountingCustomerParty () != null &&
        aUBLInvoice.getAccountingCustomerParty ().getParty () != null &&
        !aUBLInvoice.getAccountingCustomerParty ().getParty ().getPartyLegalEntity ().isEmpty () &&
        aUBLInvoice.getAccountingCustomerParty ().getParty ().getPartyLegalEntity ().get (0).getRegistrationAddress () != null)
    {
      final TradeAddressType aBTAT = new TradeAddressType ();
      aBTAT.setCityName (aUBLInvoice.getAccountingCustomerParty ()
                                    .getParty ()
                                    .getPartyLegalEntity ()
                                    .get (0)
                                    .getRegistrationAddress ()
                                    .getCityName ()
                                    .getValue ());
      if (aUBLInvoice.getAccountingCustomerParty ()
                     .getParty ()
                     .getPartyLegalEntity ()
                     .get (0)
                     .getRegistrationAddress ()
                     .getCountry () != null)
      {
        aBTAT.setCountryID (aUBLInvoice.getAccountingCustomerParty ()
                                       .getParty ()
                                       .getPartyLegalEntity ()
                                       .get (0)
                                       .getRegistrationAddress ()
                                       .getCountry ()
                                       .getIdentificationCode ()
                                       .getValue ());
      }
      aBTAT.setCountrySubDivisionName (_convertTextType (aUBLInvoice.getAccountingCustomerParty ()
                                                                    .getParty ()
                                                                    .getPartyLegalEntity ()
                                                                    .get (0)
                                                                    .getRegistrationAddress ()
                                                                    .getCountrySubentity ()
                                                                    .getValue ()));
      aBLOT.setPostalTradeAddress (aBTAT);
    }

    aBTPT.setSpecifiedLegalOrganization (aBLOT);

    if (aUBLInvoice.getAccountingCustomerParty () != null &&
        aUBLInvoice.getAccountingCustomerParty ().getParty () != null &&
        aUBLInvoice.getAccountingCustomerParty ().getParty ().getPostalAddress () != null)
    {
      final TradeAddressType aBTATR = new TradeAddressType ();
      if (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPostalAddress ().getPostalZone () != null)
      {
        aBTATR.setPostcodeCode (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPostalAddress ().getPostalZone ().getValue ());
      }
      if (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPostalAddress ().getStreetName () != null)
      {
        aBTATR.setLineOne (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPostalAddress ().getStreetName ().getValue ());
      }
      if (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPostalAddress ().getAdditionalStreetName () != null)
      {
        aBTATR.setLineTwo (aUBLInvoice.getAccountingCustomerParty ()
                                      .getParty ()
                                      .getPostalAddress ()
                                      .getAdditionalStreetName ()
                                      .getValue ());
      }
      if (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPostalAddress ().getCityName () != null)
      {
        aBTATR.setCityName (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPostalAddress ().getCityName ().getValue ());
      }
      if (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPostalAddress ().getCountry () != null)
      {
        aBTATR.setCountryID (aUBLInvoice.getAccountingCustomerParty ()
                                        .getParty ()
                                        .getPostalAddress ()
                                        .getCountry ()
                                        .getIdentificationCode ()
                                        .getValue ());
      }
      if (aUBLInvoice.getAccountingCustomerParty ().getParty ().getPostalAddress ().getCountrySubentity () != null)
      {
        aBTATR.setCountrySubDivisionName (_convertTextType (aUBLInvoice.getAccountingCustomerParty ()
                                                                       .getParty ()
                                                                       .getPostalAddress ()
                                                                       .getCountrySubentity ()
                                                                       .getValue ()));
      }
      aBTPT.setPostalTradeAddress (aBTATR);
    }

    if (aUBLInvoice.getAccountingCustomerParty () != null &&
        aUBLInvoice.getAccountingCustomerParty ().getParty () != null &&
        aUBLInvoice.getAccountingCustomerParty ().getParty ().getEndpointID () != null)
    {
      final List <UniversalCommunicationType> aLstBUCT = new ArrayList <> ();
      final UniversalCommunicationType aBUCT = new UniversalCommunicationType ();
      aBUCT.setURIID (_convertIDType (aUBLInvoice.getAccountingCustomerParty ().getParty ().getEndpointID ()));
      aLstBUCT.add (aBUCT);
      aBTPT.setURIUniversalCommunication (aLstBUCT);
    }

    if (aUBLInvoice.getAccountingCustomerParty () != null &&
        aUBLInvoice.getAccountingCustomerParty ().getParty () != null &&
        !aUBLInvoice.getAccountingCustomerParty ().getParty ().getPartyTaxScheme ().isEmpty ())
    {
      final List <TaxRegistrationType> aLstBTRT = new ArrayList <> ();
      final TaxRegistrationType aBTRT = new TaxRegistrationType ();
      aBTRT.setID (_convertIDType (aUBLInvoice.getAccountingCustomerParty ()
                                              .getParty ()
                                              .getPartyTaxScheme ()
                                              .get (0)
                                              .getTaxScheme ()
                                              .getID ()));
      aLstBTRT.add (aBTRT);
      aBTPT.setSpecifiedTaxRegistration (aLstBTRT);
    }
    return aBTPT;
  }

  @Nonnull
  private static HeaderTradeDeliveryType _convertApplicableHeaderTradeDelivery (final InvoiceType aUBLInvoice)
  {
    final HeaderTradeDeliveryType aHTDT = new HeaderTradeDeliveryType ();
    final TradePartyType aTPTHT = new TradePartyType ();

    if (!aUBLInvoice.getDelivery ().isEmpty () &&
        aUBLInvoice.getDelivery ().get (0).getDeliveryLocation () != null &&
        aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getID () != null)
    {
      final List <IDType> aLstHTIT = new ArrayList <> ();
      aLstHTIT.add (_convertIDType (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getID ()));
      aTPTHT.setID (aLstHTIT);
    }

    if (!aUBLInvoice.getDelivery ().isEmpty () &&
        aUBLInvoice.getDelivery ().get (0).getDeliveryLocation () != null &&
        aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress () != null)
    {
      final TradeAddressType aHTTAT = new TradeAddressType ();
      if (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getPostalZone () != null)
      {
        aHTTAT.setPostcodeCode (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getPostalZone ().getValue ());
      }
      if (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getStreetName () != null)
      {
        aHTTAT.setLineOne (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getStreetName ().getValue ());
      }
      if (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getAdditionalStreetName () != null)
      {
        aHTTAT.setLineTwo (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getAdditionalStreetName ().getValue ());
      }
      if (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getCityName () != null)
      {
        aHTTAT.setCityName (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getCityName ().getValue ());
      }
      if (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getCountry () != null &&
          aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getCountry ().getIdentificationCode () != null)
      {
        aHTTAT.setCountryID (aUBLInvoice.getDelivery ()
                                        .get (0)
                                        .getDeliveryLocation ()
                                        .getAddress ()
                                        .getCountry ()
                                        .getIdentificationCode ()
                                        .getValue ());
      }
      if (aUBLInvoice.getDelivery ().get (0).getDeliveryLocation ().getAddress ().getCountrySubentityCodeValue () != null)
      {
        aHTTAT.setCountrySubDivisionName (_convertTextType (aUBLInvoice.getDelivery ()
                                                                       .get (0)
                                                                       .getDeliveryLocation ()
                                                                       .getAddress ()
                                                                       .getCountrySubentityCodeValue ()));
      }
      aTPTHT.setPostalTradeAddress (aHTTAT);
      aHTDT.setShipToTradeParty (aTPTHT);
    }

    if (!aUBLInvoice.getDelivery ().isEmpty () && aUBLInvoice.getDelivery ().get (0).getActualDeliveryDate () != null)
    {
      final SupplyChainEventType aSCET = new SupplyChainEventType ();
      aSCET.setOccurrenceDateTime (_createDate (aUBLInvoice.getDelivery ().get (0).getActualDeliveryDate ().getValueLocal ()));
      aHTDT.setActualDeliverySupplyChainEvent (aSCET);
    }
    return aHTDT;
  }

  @Nonnull
  private static HeaderTradeSettlementType _convertApplicableHeaderTradeSettlement (final InvoiceType aUBLInvoice)
  {
    final HeaderTradeSettlementType aHTST = new HeaderTradeSettlementType ();
    if (!aUBLInvoice.getPaymentMeans ().isEmpty () && !aUBLInvoice.getPaymentMeans ().get (0).getPaymentID ().isEmpty ())
    {
      aHTST.setPaymentReference (_convertTextType (aUBLInvoice.getPaymentMeans ().get (0).getPaymentID ().get (0).getValue ()));
    }

    aHTST.setInvoiceCurrencyCode (aUBLInvoice.getDocumentCurrencyCode ().getValue ());
    aHTST.setPayeeTradeParty (_convertPayeeTradeParty (aUBLInvoice));

    if (!aUBLInvoice.getPaymentMeans ().isEmpty ())
    {
      final List <TradeSettlementPaymentMeansType> aLstTSPMT = new ArrayList <> ();
      final TradeSettlementPaymentMeansType aTSPMT = new TradeSettlementPaymentMeansType ();
      aTSPMT.setTypeCode (aUBLInvoice.getPaymentMeans ().get (0).getPaymentMeansCodeValue ());
      final CreditorFinancialAccountType aCFAT = new CreditorFinancialAccountType ();
      if (aUBLInvoice.getPaymentMeans ().get (0).getPayeeFinancialAccount () != null)
      {
        aCFAT.setIBANID (aUBLInvoice.getPaymentMeans ().get (0).getPayeeFinancialAccount ().getIDValue ());
      }
      aTSPMT.setPayeePartyCreditorFinancialAccount (aCFAT);
      aLstTSPMT.add (aTSPMT);
      aHTST.setSpecifiedTradeSettlementPaymentMeans (aLstTSPMT);
    }

    aHTST.setApplicableTradeTax (_convertApplicableTradeTax (aUBLInvoice));

    if (!aUBLInvoice.getInvoicePeriod ().isEmpty ())
    {
      final SpecifiedPeriodType aSPT = new SpecifiedPeriodType ();
      if (aUBLInvoice.getInvoicePeriod ().get (0).getStartDate () != null)
      {
        aSPT.setStartDateTime (_createDate (aUBLInvoice.getInvoicePeriod ().get (0).getStartDate ().getValueLocal ()));
      }
      if (aUBLInvoice.getInvoicePeriod ().get (0).getEndDate () != null)
      {
        aSPT.setEndDateTime (_createDate (aUBLInvoice.getInvoicePeriod ().get (0).getEndDate ().getValueLocal ()));
      }
      aHTST.setBillingSpecifiedPeriod (aSPT);
    }

    aHTST.setSpecifiedTradeAllowanceCharge (_convertSpecifiedTradeAllowanceCharge (aUBLInvoice));
    aHTST.setSpecifiedTradePaymentTerms (_convertSpecifiedTradePaymentTerms (aUBLInvoice));
    aHTST.setSpecifiedTradeSettlementHeaderMonetarySummation (_convertSpecifiedTradeSettlementHeaderMonetarySummation (aUBLInvoice));

    final List <TradeAccountingAccountType> aLstTAAT = new ArrayList <> ();
    if (aUBLInvoice.getAccountingCost () != null)
    {
      final TradeAccountingAccountType aTAAT = new TradeAccountingAccountType ();
      aTAAT.setID (aUBLInvoice.getAccountingCost ().getValue ());
      aLstTAAT.add (aTAAT);
    }
    aHTST.setReceivableSpecifiedTradeAccountingAccount (aLstTAAT);

    return aHTST;
  }

  @Nonnull
  private static TradePartyType _convertPayeeTradeParty (final InvoiceType aUBLInvoice)
  {
    final TradePartyType aSTTPT = new TradePartyType ();
    if (aUBLInvoice.getPayeeParty () != null && !aUBLInvoice.getPayeeParty ().getPartyIdentification ().isEmpty ())
    {
      final List <IDType> aLstSIT = new ArrayList <> ();
      aLstSIT.add (_convertIDType (aUBLInvoice.getPayeeParty ().getPartyIdentification ().get (0).getID ()));
      aSTTPT.setID (aLstSIT);
    }

    if (aUBLInvoice.getPayeeParty () != null && !aUBLInvoice.getPayeeParty ().getPartyName ().isEmpty ())
    {
      aSTTPT.setName (aUBLInvoice.getPayeeParty ().getPartyName ().get (0).getName ().getValue ());
    }

    if (aUBLInvoice.getPayeeParty () != null && !aUBLInvoice.getPayeeParty ().getPartyLegalEntity ().isEmpty ())
    {
      final LegalOrganizationType aSTLOT = new LegalOrganizationType ();
      aSTLOT.setID (_convertIDType (aUBLInvoice.getPayeeParty ().getPartyLegalEntity ().get (0).getCompanyID ()));
      aSTTPT.setSpecifiedLegalOrganization (aSTLOT);
    }
    return aSTTPT;
  }

  @Nonnull
  private static TradeSettlementHeaderMonetarySummationType _convertSpecifiedTradeSettlementHeaderMonetarySummation (final InvoiceType aUBLInvoice)
  {
    final TradeSettlementHeaderMonetarySummationType aTSHMST = new TradeSettlementHeaderMonetarySummationType ();
    final MonetaryTotalType aUBLLMT = aUBLInvoice.getLegalMonetaryTotal ();
    if (aUBLLMT != null)
    {
      if (aUBLLMT.getLineExtensionAmount () != null)
      {
        aTSHMST.setLineTotalAmount (_convertAmountType (aUBLLMT.getLineExtensionAmount ()));
      }
      if (aUBLLMT.getChargeTotalAmount () != null)
      {
        aTSHMST.setChargeTotalAmount (_convertAmountType (aUBLLMT.getChargeTotalAmount ()));
      }
      if (aUBLLMT.getAllowanceTotalAmount () != null)
      {
        aTSHMST.setAllowanceTotalAmount (_convertAmountType (aUBLLMT.getAllowanceTotalAmount ()));
      }
      if (aUBLLMT.getTaxExclusiveAmount () != null)
      {
        aTSHMST.setTaxBasisTotalAmount (_convertAmountType (aUBLLMT.getTaxExclusiveAmount ()));
      }
    }

    if (!aUBLInvoice.getTaxTotal ().isEmpty () && aUBLInvoice.getTaxTotal ().get (0).getTaxAmount () != null)
    {
      aTSHMST.setTaxTotalAmount (_convertAmountType (aUBLInvoice.getTaxTotal ().get (0).getTaxAmount ()));
    }

    if (aUBLLMT != null)
    {
      if (aUBLLMT.getPayableRoundingAmount () != null)
      {
        aTSHMST.setRoundingAmount (_convertAmountType (aUBLLMT.getPayableRoundingAmount ()));
      }
      if (aUBLLMT.getTaxInclusiveAmount () != null)
      {
        aTSHMST.setGrandTotalAmount (_convertAmountType (aUBLLMT.getTaxInclusiveAmount ()));
      }
      if (aUBLLMT.getPrepaidAmount () != null)
      {
        aTSHMST.setTotalPrepaidAmount (_convertAmountType (aUBLLMT.getPrepaidAmount ()));
      }
      if (aUBLLMT.getPayableAmount () != null)
      {
        aTSHMST.setDuePayableAmount (_convertAmountType (aUBLLMT.getPayableAmount ()));
      }
    }

    return aTSHMST;
  }

  @Nonnull
  private static List <TradeTaxType> _convertApplicableTradeTax (final InvoiceType aUBLInvoice)
  {
    final List <TradeTaxType> aLstTTTST = new ArrayList <> ();
    for (final TaxTotalType aTTT : aUBLInvoice.getTaxTotal ())
      if (!aTTT.getTaxSubtotal ().isEmpty ())
      {
        final TaxSubtotalType aFirst = aTTT.getTaxSubtotal ().get (0);
        final TradeTaxType aTTTST = new TradeTaxType ();
        if (aFirst.getTaxAmount () != null)
        {
          aTTTST.setCalculatedAmount (_convertAmountType (aFirst.getTaxAmount ()));
        }
        if (aFirst.getTaxCategory () != null)
        {
          aTTTST.setTypeCode (aFirst.getTaxCategory ().getIDValue ());
        }
        if (aFirst.getTaxableAmount () != null)
        {
          aTTTST.setBasisAmount (_convertAmountType (aFirst.getTaxableAmount ()));
        }

        if (aFirst.getTaxCategory () != null)
        {
          if (!aFirst.getTaxCategory ().getTaxExemptionReason ().isEmpty ())
          {
            aTTTST.setExemptionReason (aFirst.getTaxCategory ().getTaxExemptionReason ().get (0).getValue ());
          }
          if (aFirst.getTaxCategory ().getTaxExemptionReasonCode () != null)
          {
            aTTTST.setExemptionReasonCode (aFirst.getTaxCategory ().getTaxExemptionReasonCode ().getValue ());
          }
        }
        aLstTTTST.add (aTTTST);
      }
    return aLstTTTST;
  }

  @Nonnull
  private static List <TradePaymentTermsType> _convertSpecifiedTradePaymentTerms (final InvoiceType aUBLInvoice)
  {
    final List <TradePaymentTermsType> aLstTPTT = new ArrayList <> ();
    for (final PaymentTermsType aPTT : aUBLInvoice.getPaymentTerms ())
    {
      final TradePaymentTermsType aTPTT = new TradePaymentTermsType ();
      if (!aPTT.getNote ().isEmpty ())
      {
        aTPTT.setDescription (_convertTextType (aPTT.getNote ().get (0).getValue ()));
      }
      if (!aUBLInvoice.getPaymentMeans ().isEmpty () && aUBLInvoice.getPaymentMeans ().get (0).getPaymentDueDate () != null)
      {
        aTPTT.setDueDateDateTime (_createDate (aUBLInvoice.getPaymentMeans ().get (0).getPaymentDueDate ().getValueLocal ()));
      }
      aLstTPTT.add (aTPTT);
    }
    return aLstTPTT;
  }

  @Nonnull
  private static List <TradeAllowanceChargeType> _convertSpecifiedTradeAllowanceCharge (final InvoiceType aUBLInvoice)
  {
    final List <TradeAllowanceChargeType> aLstTDCT = new ArrayList <> ();
    for (final AllowanceChargeType aACT : aUBLInvoice.getAllowanceCharge ())
    {
      final TradeAllowanceChargeType aTDCT = new TradeAllowanceChargeType ();
      final IndicatorType aITDC = new IndicatorType ();
      aITDC.setIndicator (Boolean.valueOf (aACT.getChargeIndicator ().isValue ()));
      aTDCT.setChargeIndicator (aITDC);

      final List <AmountType> aLstATDC = new ArrayList <> ();
      final AmountType aATCD = new AmountType ();
      aATCD.setValue (aACT.getAmount ().getValue ());
      aLstATDC.add (aATCD);
      aTDCT.setActualAmount (aLstATDC);

      if (!aACT.getAllowanceChargeReason ().isEmpty ())
      {
        aTDCT.setReason (aACT.getAllowanceChargeReason ().get (0).getValue ());
      }
      aLstTDCT.add (aTDCT);
    }
    return aLstTDCT;
  }

  @Nonnull
  private static List <ReferencedDocumentType> _convertAdditionalReferencedDocument (final InvoiceType aUBLInvoice)
  {
    final List <ReferencedDocumentType> aLstRDT = new ArrayList <> ();
    final List <BinaryObjectType> aLstBOT = new ArrayList <> ();
    for (final DocumentReferenceType aDRT : aUBLInvoice.getAdditionalDocumentReference ())
    {
      final ReferencedDocumentType aURDT = new ReferencedDocumentType ();
      aURDT.setIssuerAssignedID (aDRT.getID ().getValue ());
      if (aDRT.getAttachment () != null &&
          aDRT.getAttachment ().getExternalReference () != null &&
          aDRT.getAttachment ().getExternalReference ().getURI () != null)
      {
        aURDT.setURIID (aDRT.getAttachment ().getExternalReference ().getURI ().getValue ());
      }
      final BinaryObjectType aBOT = new BinaryObjectType ();
      if (aDRT.getAttachment () != null && aDRT.getAttachment ().getEmbeddedDocumentBinaryObject () != null)
      {
        aBOT.setMimeCode (aDRT.getAttachment ().getEmbeddedDocumentBinaryObject ().getMimeCode ());
        aBOT.setValue (aDRT.getAttachment ().getEmbeddedDocumentBinaryObject ().getValue ());
      }
      aLstBOT.add (aBOT);
      aLstRDT.add (aURDT);
    }
    return aLstRDT;
  }

  @Nullable
  public static CrossIndustryInvoiceType convertToCrossIndustryInvoice (@Nonnull final InvoiceType aUBLInvoice,
                                                                        @Nonnull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aUBLInvoice, "UBLInvoice");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    final ExchangedDocumentContextType aEDCT = new ExchangedDocumentContextType ();
    if (aUBLInvoice.getCustomizationID () != null)
    {
      final DocumentContextParameterType aDCP = new DocumentContextParameterType ();
      aDCP.setID (aUBLInvoice.getCustomizationIDValue ());
      aEDCT.addGuidelineSpecifiedDocumentContextParameter (aDCP);
    }

    final ExchangedDocumentType aEDT = new ExchangedDocumentType ();

    aEDT.setID (aUBLInvoice.getIDValue ());
    aEDT.setTypeCode (aUBLInvoice.getInvoiceTypeCodeValue ());

    // IssueDate
    if (aUBLInvoice.getIssueDate () != null)
    {
      aEDT.setIssueDateTime (_createDate (aUBLInvoice.getIssueDate ().getValueLocal ()));
    }

    // IncludedNote
    if (!aUBLInvoice.getNote ().isEmpty ())
    {
      aEDT.setIncludedNote (_convertIncludedNote (aUBLInvoice.getNote ().get (0)));
    }

    final SupplyChainTradeTransactionType aSCTT = new SupplyChainTradeTransactionType ();

    // IncludedSupplyChainTradeLineItem
    aSCTT.setIncludedSupplyChainTradeLineItem (_convertInvoiceLine (aUBLInvoice.getInvoiceLine ()));

    // ApplicableHeaderTradeAgreement
    final HeaderTradeAgreementType aHTAT = new HeaderTradeAgreementType ();

    // SellerTradeParty
    aHTAT.setSellerTradeParty (_convertSellerTradeParty (aUBLInvoice));

    // BuyerTradeParty
    aHTAT.setBuyerTradeParty (_convertBuyerTradeParty (aUBLInvoice));

    // BuyerOrderReferencedDocument
    final ReferencedDocumentType aRDT = new ReferencedDocumentType ();
    if (aUBLInvoice.getOrderReference () != null)
    {
      aRDT.setIssuerAssignedID (aUBLInvoice.getOrderReference ().getID ().getValue ());
    }
    aHTAT.setBuyerOrderReferencedDocument (aRDT);

    // ContractReferencedDocument
    if (!aUBLInvoice.getContractDocumentReference ().isEmpty ())
    {
      final ReferencedDocumentType aCRDT = new ReferencedDocumentType ();
      if (!aUBLInvoice.getContractDocumentReference ().isEmpty ())
      {
        aCRDT.setIssuerAssignedID (aUBLInvoice.getContractDocumentReference ().get (0).getID ().getValue ());
      }
      aHTAT.setContractReferencedDocument (aCRDT);
    }

    // AdditionalReferencedDocument
    aHTAT.setAdditionalReferencedDocument (_convertAdditionalReferencedDocument (aUBLInvoice));
    aSCTT.setApplicableHeaderTradeAgreement (aHTAT);

    // ApplicableHeaderTradeDelivery
    aSCTT.setApplicableHeaderTradeDelivery (_convertApplicableHeaderTradeDelivery (aUBLInvoice));

    // ApplicableHeaderTradeSettlement
    aSCTT.setApplicableHeaderTradeSettlement (_convertApplicableHeaderTradeSettlement (aUBLInvoice));

    final CrossIndustryInvoiceType aCIIInvoice = new CrossIndustryInvoiceType ();
    aCIIInvoice.setExchangedDocumentContext (aEDCT);
    aCIIInvoice.setExchangedDocument (aEDT);
    aCIIInvoice.setSupplyChainTradeTransaction (aSCTT);

    return aCIIInvoice;
  }
}

