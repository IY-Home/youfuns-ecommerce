package com.youfuns.ecommerce.frontend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.youfuns.ecommerce.frontend.payloads.*;
import com.youfuns.paramtypes.Address;

import java.io.IOException;
import java.io.InputStream;

public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtils() {
        // Utility class
    }

    // ============= DESERIALIZATION =============

    public static LoginUserPayload parseLoginPayload(String json) throws IOException {
        return MAPPER.readValue(json, LoginUserPayload.class);
    }

    public static LoginUserPayload parseLoginPayload(InputStream jsonStream) throws IOException {
        return MAPPER.readValue(jsonStream, LoginUserPayload.class);
    }

    public static RegisterUserPayload parseRegisterPayload(String json) throws IOException {
        return MAPPER.readValue(json, RegisterUserPayload.class);
    }

    public static RegisterUserPayload parseRegisterPayload(InputStream jsonStream) throws IOException {
        return MAPPER.readValue(jsonStream, RegisterUserPayload.class);
    }

    public static UpdateProfilePayload parseUpdateProfilePayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateProfilePayload.class);
    }

    public static ChangePasswordPayload parseChangePasswordPayload(String json) throws IOException {
        return MAPPER.readValue(json, ChangePasswordPayload.class);
    }

    public static AdminChangePasswordPayload parseAdminChangePasswordPayload(String json) throws IOException {
        return MAPPER.readValue(json, AdminChangePasswordPayload.class);
    }

    public static AddUsernamePayload parseAddUsernamePayload(String json) throws IOException {
        return MAPPER.readValue(json, AddUsernamePayload.class);
    }

    public static RemoveUsernamePayload parseRemoveUsernamePayload(String json) throws IOException {
        return MAPPER.readValue(json, RemoveUsernamePayload.class);
    }

    public static AdminUpdateProfilePayload parseAdminUpdateProfilePayload(String json) throws IOException {
        return MAPPER.readValue(json, AdminUpdateProfilePayload.class);
    }

    public static AddToCartPayload parseAddToCartPayload(String json) throws IOException {
        return MAPPER.readValue(json, AddToCartPayload.class);
    }

    public static RemoveFromCartPayload parseRemoveFromCartPayload(String json) throws IOException {
        return MAPPER.readValue(json, RemoveFromCartPayload.class);
    }

    public static UpdateCartQuantityPayload parseUpdateCartQuantityPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateCartQuantityPayload.class);
    }

    public static AddToWishlistPayload parseAddToWishlistPayload(String json) throws IOException {
        return MAPPER.readValue(json, AddToWishlistPayload.class);
    }

    public static RemoveFromWishlistPayload parseRemoveFromWishlistPayload(String json) throws IOException {
        return MAPPER.readValue(json, RemoveFromWishlistPayload.class);
    }

    public static CreateOrderPayload parseCreateOrderPayload(String json) throws IOException {
        return MAPPER.readValue(json, CreateOrderPayload.class);
    }

    public static Address parseAddress(String json) throws IOException {
        return MAPPER.readValue(json, Address.class);
    }

    public static RegisterVendorPayload parseRegisterVendorPayload(String json) throws IOException {
        return MAPPER.readValue(json, RegisterVendorPayload.class);
    }

    // In JsonUtils.java

    public static UpdateVendorShopNamePayload parseUpdateVendorShopNamePayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorShopNamePayload.class);
    }
    public static UpdateVendorShopDescriptionPayload parseUpdateVendorShopDescriptionPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorShopDescriptionPayload.class);
    }
    public static UpdateVendorCategoryPayload parseUpdateVendorCategoryPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorCategoryPayload.class);
    }
    public static UpdateVendorShopLogoPayload parseUpdateVendorShopLogoPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorShopLogoPayload.class);
    }
    public static UpdateVendorShopBannerPayload parseUpdateVendorShopBannerPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorShopBannerPayload.class);
    }
    public static UpdateVendorShopTaglinePayload parseUpdateVendorShopTaglinePayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorShopTaglinePayload.class);
    }
    public static UpdateVendorBusinessEmailPayload parseUpdateVendorBusinessEmailPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorBusinessEmailPayload.class);
    }
    public static UpdateVendorBusinessPhonePayload parseUpdateVendorBusinessPhonePayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorBusinessPhonePayload.class);
    }
    public static UpdateVendorWebsiteUrlPayload parseUpdateVendorWebsiteUrlPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorWebsiteUrlPayload.class);
    }
    public static UpdateVendorAddressPayload parseUpdateVendorAddressPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorAddressPayload.class);
    }
    public static UpdateVendorSocialLinkPayload parseUpdateVendorSocialLinkPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorSocialLinkPayload.class);
    }
    public static UpdateVendorTaxIdPayload parseUpdateVendorTaxIdPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorTaxIdPayload.class);
    }
    public static UpdateVendorBusinessRegistrationPayload parseUpdateVendorBusinessRegistrationPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorBusinessRegistrationPayload.class);
    }
    public static UpdateVendorBusinessTypePayload parseUpdateVendorBusinessTypePayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorBusinessTypePayload.class);
    }
    public static UpdateVendorLegalNamePayload parseUpdateVendorLegalNamePayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorLegalNamePayload.class);
    }
    public static UpdateVendorYearEstablishedPayload parseUpdateVendorYearEstablishedPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorYearEstablishedPayload.class);
    }
    public static UpdateVendorStoreThemePayload parseUpdateVendorStoreThemePayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorStoreThemePayload.class);
    }
    public static UpdateVendorStoreLanguagePayload parseUpdateVendorStoreLanguagePayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorStoreLanguagePayload.class);
    }
    public static UpdateVendorStoreCurrencyPayload parseUpdateVendorStoreCurrencyPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorStoreCurrencyPayload.class);
    }
    public static UpdateVendorStoreVisibilityPayload parseUpdateVendorStoreVisibilityPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorStoreVisibilityPayload.class);
    }
    public static UpdateVendorReturnPolicyPayload parseUpdateVendorReturnPolicyPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorReturnPolicyPayload.class);
    }
    public static UpdateVendorShippingPolicyPayload parseUpdateVendorShippingPolicyPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorShippingPolicyPayload.class);
    }
    public static UpdateVendorShopImagesPayload parseUpdateVendorShopImagesPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateVendorShopImagesPayload.class);
    }
    public static ProductCreatePayload parseProductCreatePayload(String json) throws IOException {
        return MAPPER.readValue(json, ProductCreatePayload.class);
    }

    public static UpdateProductPayload parseUpdateProductPayload(String json) throws IOException {
        return MAPPER.readValue(json, UpdateProductPayload.class);
    }

    public static <T> T parsePayload(String json, Class<T> type) throws IOException {
        return MAPPER.readValue(json, type);
    }

    public static <T> T parsePayload(InputStream jsonStream, Class<T> type) throws IOException {
        return MAPPER.readValue(jsonStream, type);
    }

    // ============= SERIALIZATION =============

    public static String toJson(ResultReturn result) throws JsonProcessingException {
        return MAPPER.writeValueAsString(result);
    }

    public static String toJson(ResultPayload<?> payload) throws JsonProcessingException {
        return MAPPER.writeValueAsString(payload);
    }

    public static <T> String toJson(T object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public static byte[] toJsonBytes(ResultReturn result) throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(result);
    }

    public static byte[] toJsonBytes(ResultPayload<?> payload) throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(payload);
    }

    public static <T> byte[] toJsonBytes(T object) throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(object);
    }

    // ============= CONVENIENCE METHODS =============

    public static String successResult(String message) throws JsonProcessingException {
        return toJson(new ResultReturn(ResultReturn.Result.SUCCESS, message));
    }

    public static String failureResult(String message) throws JsonProcessingException {
        return toJson(new ResultReturn(ResultReturn.Result.FAILURE, message));
    }

    public static <T> String successPayload(T payload, String message) throws JsonProcessingException {
        return toJson(new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.SUCCESS, message),
                payload
        ));
    }

    public static <T> String failurePayload(String message) throws JsonProcessingException {
        return toJson(new ResultPayload<>(
                new ResultReturn(ResultReturn.Result.FAILURE, message),
                null
        ));
    }

    public static boolean isValidJson(String json) {
        try {
            MAPPER.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}