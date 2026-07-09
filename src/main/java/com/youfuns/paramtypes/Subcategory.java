package com.youfuns.paramtypes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Subcategory {
    // ===== ELECTRONICS =====
    SMARTPHONES(Category.ELECTRONICS),
    LAPTOPS(Category.ELECTRONICS),
    TABLETS(Category.ELECTRONICS),
    DESKTOPS(Category.ELECTRONICS),
    MONITORS(Category.ELECTRONICS),
    PRINTERS(Category.ELECTRONICS),
    STORAGE(Category.ELECTRONICS),
    ACCESSORIES(Category.ELECTRONICS),
    WEARABLES(Category.ELECTRONICS),
    AUDIO(Category.ELECTRONICS),
    CAMERAS(Category.ELECTRONICS),
    GAMING(Category.ELECTRONICS),
    TV(Category.ELECTRONICS),
    HOME_APPLIANCES(Category.ELECTRONICS),
    KITCHEN_APPLIANCES(Category.ELECTRONICS),

    // ===== CLOTHING =====
    MEN(Category.CLOTHING),
    WOMEN(Category.CLOTHING),
    KIDS(Category.CLOTHING),
    BABY(Category.CLOTHING),
    SHOES(Category.CLOTHING),
    SNEAKERS(Category.CLOTHING),
    BOOTS(Category.CLOTHING),
    SANDALS(Category.CLOTHING),
    FASHION_ACCESSORIES(Category.CLOTHING),
    JEWELRY(Category.CLOTHING),
    WATCHES(Category.CLOTHING),
    LUGGAGE(Category.CLOTHING),
    HANDBAGS(Category.CLOTHING),
    BACKPACKS(Category.CLOTHING),
    WALLETS(Category.CLOTHING),
    SUNGLASSES(Category.CLOTHING),
    HAIR_ACCESSORIES(Category.CLOTHING),
    MASKS(Category.CLOTHING),

    // ===== HOME & LIVING =====
    FURNITURE(Category.HOME),
    BEDROOM(Category.HOME),
    LIVING_ROOM(Category.HOME),
    DINING(Category.HOME),
    OFFICE(Category.HOME),
    OUTDOOR(Category.HOME),
    DECOR(Category.HOME),
    LIGHTING(Category.HOME),
    RUGS(Category.HOME),
    CURTAINS(Category.HOME),
    BEDDING(Category.HOME),
    TOWELS(Category.HOME),
    KITCHEN(Category.HOME),
    COOKWARE(Category.HOME),
    CUTLERY(Category.HOME),
    DISHES(Category.HOME),
    GLASSWARE(Category.HOME),
    STORAGE_HOME(Category.HOME),
    CLEANING(Category.HOME),
    TOOLS(Category.HOME),
    HARDWARE(Category.HOME),
    PAINT(Category.HOME),

    // ===== BOOKS & MEDIA =====
    BOOKS(Category.BOOKS),
    EBOOKS(Category.BOOKS),
    AUDIOBOOKS(Category.BOOKS),
    MAGAZINES(Category.BOOKS),
    COMICS(Category.BOOKS),
    MUSIC(Category.BOOKS),
    VINYL(Category.BOOKS),
    MOVIES(Category.BOOKS),
    GAMES(Category.BOOKS),
    SOFTWARE(Category.BOOKS),

    // ===== BEAUTY & HEALTH =====
    SKINCARE(Category.BEAUTY),
    MAKEUP(Category.BEAUTY),
    HAIRCARE(Category.BEAUTY),
    BATH_BODY(Category.BEAUTY),
    FRAGRANCE(Category.BEAUTY),
    NAILS(Category.BEAUTY),
    HEALTH(Category.BEAUTY),
    VITAMINS(Category.BEAUTY),
    MEDICAL(Category.BEAUTY),
    FITNESS(Category.BEAUTY),
    WELLNESS(Category.BEAUTY),
    BABY_CARE(Category.BEAUTY),

    // ===== FOOD & GROCERY =====
    FRESH(Category.GROCERY),
    PACKAGED(Category.GROCERY),
    CEREALS(Category.GROCERY),
    SNACKS(Category.GROCERY),
    BEVERAGES(Category.GROCERY),
    COFFEE_TEA(Category.GROCERY),
    BAKING(Category.GROCERY),
    CANNED(Category.GROCERY),
    FROZEN(Category.GROCERY),
    DAIRY(Category.GROCERY),
    MEAT(Category.GROCERY),
    SEAFOOD(Category.GROCERY),
    OILS(Category.GROCERY),
    SPICES(Category.GROCERY),
    ORGANIC(Category.GROCERY),
    GOURMET(Category.GROCERY),
    PET_FOOD(Category.GROCERY),

    // ===== VEHICLES & PARTS =====
    VEHICLES(Category.VEHICLES),
    PARTS(Category.VEHICLES),
    TIRES(Category.VEHICLES),
    OIL(Category.VEHICLES),
    TOOLS_AUTO(Category.VEHICLES),
    ACCESSORIES_AUTO(Category.VEHICLES),

    // ===== SPORTS & OUTDOORS =====
    OUTDOORS(Category.SPORTS),
    CAMPING(Category.SPORTS),
    HIKING(Category.SPORTS),
    CLIMBING(Category.SPORTS),
    FISHING(Category.SPORTS),
    HUNTING(Category.SPORTS),
    BIKING(Category.SPORTS),
    SKIING(Category.SPORTS),
    GOLF(Category.SPORTS),
    TENNIS(Category.SPORTS),
    SWIMMING(Category.SPORTS),
    YOGA(Category.SPORTS),
    TEAM_SPORTS(Category.SPORTS),

    // ===== TOYS & HOBBIES =====
    DOLLS(Category.TOYS),
    ACTION_FIGURES(Category.TOYS),
    PUZZLES(Category.TOYS),
    LEGO(Category.TOYS),
    BOARD_GAMES(Category.TOYS),
    CARD_GAMES(Category.TOYS),
    RC(Category.TOYS),
    ARTS_CRAFTS(Category.TOYS),
    EDUCATIONAL(Category.TOYS),
    STUFFED(Category.TOYS),
    MODEL_KITS(Category.TOYS),
    SCIENCE_KITS(Category.TOYS),

    // ===== MISCELLANEOUS =====
    GIFTS(Category.MISC),
    HOLIDAY(Category.MISC),
    SEASONAL(Category.MISC),
    ECO_FRIENDLY(Category.MISC),
    HANDMADE(Category.MISC),
    VINTAGE(Category.MISC),
    COLLECTIBLES(Category.MISC),
    ADULT(Category.MISC),
    PET_SUPPLIES(Category.MISC),
    OFFICE_SUPPLIES(Category.MISC),
    SCHOOL_SUPPLIES(Category.MISC),
    PARTY_SUPPLIES(Category.MISC);

    private final Category parentCategory;

    Subcategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public String getDisplayName() {
        return this.name().replace("_", " ").toLowerCase();
    }

    public static List<Subcategory> getByCategory(Category category) {
        return Arrays.stream(values())
                .filter(sub -> sub.parentCategory == category)
                .collect(Collectors.toList());
    }

    public static Subcategory fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}