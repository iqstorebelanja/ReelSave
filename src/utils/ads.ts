import { Capacitor } from '@capacitor/core';

/**
 * =========================================================================
 * ALLVID UNIVERSAL ADMOB & ADSENSE MONETIZATION MANAGER
 * =========================================================================
 * 
 * ⚠️ IMPORTANT - BEFORE PUBLISHING TO GOOGLE PLAY STORE:
 * Replace the TEST AdMob IDs below with your REAL AdMob Ad Unit IDs from
 * your Google AdMob Dashboard (https://admob.google.com).
 * 
 * 1. Banner ID:       Replace TEST_BANNER_ID with your Real Banner Unit ID
 * 2. Interstitial ID: Replace TEST_INTERSTITIAL_ID with your Real Interstitial ID
 * 3. Rewarded ID:     Replace TEST_REWARDED_ID with your Real Rewarded Unit ID
 * 4. App Open ID:     Replace TEST_APP_OPEN_ID with your Real App Open Unit ID
 * =========================================================================
 */

// =========================================================================
// 🟢 ADMOB AD UNIT CONFIGURATION (REPLACE TEST IDS HERE BEFORE PRODUCTION)
// =========================================================================
export const ADMOB_CONFIG = {
  // Test IDs provided by Google AdMob documentation:
  BANNER_ID: 'ca-app-pub-3940256099942544/6300978111',       // <-- REPLACE WITH REAL BANNER ID
  INTERSTITIAL_ID: 'ca-app-pub-3940256099942544/1033173712', // <-- REPLACE WITH REAL INTERSTITIAL ID
  REWARDED_ID: 'ca-app-pub-3940256099942544/5224354917',     // <-- REPLACE WITH REAL REWARDED ID
  APP_OPEN_ID: 'ca-app-pub-3940256099942544/9257395921',     // <-- REPLACE WITH REAL APP OPEN ID
  IS_TESTING: true, // Set to false when deploying to production with real IDs
};

export type QualityType = '720p' | '1080p' | '1440p' | '4K' | 'mp3' | 'jpg';

export interface UnlockQualityResult {
  success: boolean;
  quality: QualityType;
  message?: string;
  unlockedUntil?: number; // epoch ms
}

const STORAGE_PREFIX = 'allvid_quality_';
const UNLOCK_DURATION_MS = 24 * 60 * 60 * 1000; // 24 hours
export const MAX_FREE_720P_PER_DAY = 3;
let downloadSuccessCount = 0;
let isAdMobInitialized = false;

// Dynamic import holder for @capacitor-community/admob to prevent web crashes
let AdMobPlugin: any = null;

async function getAdMobPlugin(): Promise<any> {
  if (AdMobPlugin) return AdMobPlugin;
  if (!Capacitor.isNativePlatform()) return null;

  try {
    const module = await import('@capacitor-community/admob');
    AdMobPlugin = module.AdMob;
    return AdMobPlugin;
  } catch (err) {
    console.warn('[ALLVID Ads] @capacitor-community/admob not available:', err);
    return null;
  }
}

/**
 * Initialize Ads on app mount (Native AdMob or Web AdSense)
 */
export async function initializeAds(): Promise<void> {
  console.log('[ALLVID Ads] Initializing ads system...');
  const isNative = Capacitor.isNativePlatform();

  if (isNative) {
    try {
      const AdMob = await getAdMobPlugin();
      if (AdMob) {
        await AdMob.initialize({
          testingDevices: ['2077ef9a63d2b398840261c8221a0c9b'],
          initializeForTesting: ADMOB_CONFIG.IS_TESTING,
        });
        isAdMobInitialized = true;
        console.log('[ALLVID Ads] Native AdMob initialized successfully');

        // Show App Open ad and Bottom Sticky Banner on native launch
        await showAppOpen();
        await showBanner();
      }
    } catch (e) {
      console.warn('[ALLVID Ads] Native AdMob initialize error (using fallback):', e);
    }
  } else {
    // Web: Initialize Google AdSense script placeholder if needed
    console.log('[ALLVID Ads] Web platform detected: Google AdSense mode active');
  }

  // Also notify Android bridge if running in Android WebView/Activity
  if (typeof window !== 'undefined' && (window as any).AndroidAdsManager) {
    try {
      (window as any).AndroidAdsManager.initAds();
    } catch (e) {
      // Ignore
    }
  }
}

// Backward-compatible alias
export const initAds = initializeAds;

/**
 * 1. Banner Ad: sticky bottom banner on all pages
 * Native: Real AdMob Banner
 * Web: Responsive bottom banner container
 */
export async function showBanner(options?: { position?: 'BOTTOM' | 'TOP' }): Promise<void> {
  if (!Capacitor.isNativePlatform()) {
    console.log('[ALLVID Ads] Web Banner active (AdSense placeholder)');
    return;
  }

  try {
    const AdMob = await getAdMobPlugin();
    if (AdMob && isAdMobInitialized) {
      const { BannerAdSize, BannerAdPosition } = await import('@capacitor-community/admob');
      await AdMob.showBanner({
        adId: ADMOB_CONFIG.BANNER_ID,
        adSize: BannerAdSize.ADAPTIVE_BANNER,
        position: options?.position === 'TOP' ? BannerAdPosition.TOP_CENTER : BannerAdPosition.BOTTOM_CENTER,
        margin: 0,
        isTesting: ADMOB_CONFIG.IS_TESTING,
      });
      console.log('[ALLVID Ads] Native bottom banner shown');
    }
  } catch (err) {
    console.warn('[ALLVID Ads] Failed to show native banner:', err);
  }
}

export async function hideBanner(): Promise<void> {
  if (!Capacitor.isNativePlatform()) return;
  try {
    const AdMob = await getAdMobPlugin();
    if (AdMob) {
      await AdMob.hideBanner();
    }
  } catch (err) {
    console.warn('[ALLVID Ads] Hide banner error:', err);
  }
}

/**
 * 2. Interstitial Ad:
 * Shown after every 2 successful downloads, or on 1440p/4K unlock sequence
 */
export async function showInterstitial(): Promise<boolean> {
  console.log('[ALLVID Ads] Triggering Interstitial Ad...');

  if (Capacitor.isNativePlatform()) {
    try {
      const AdMob = await getAdMobPlugin();
      if (AdMob) {
        await AdMob.prepareInterstitial({
          adId: ADMOB_CONFIG.INTERSTITIAL_ID,
          isTesting: ADMOB_CONFIG.IS_TESTING,
        });
        await AdMob.showInterstitial();
        console.log('[ALLVID Ads] Real AdMob Interstitial shown successfully');
        return true;
      }
    } catch (err) {
      console.warn('[ALLVID Ads] Interstitial display error:', err);
    }
  }

  // Web or Fallback: Simulated interstitial modal delay
  console.log('[ALLVID Ads] Web / Fallback Interstitial processed');
  return true;
}

/**
 * Record a successful download. Triggers Interstitial every 2 downloads.
 */
export async function recordSuccessfulDownload(isVipActive = false): Promise<void> {
  if (isVipActive) return;
  downloadSuccessCount += 1;
  console.log(`[ALLVID Ads] Successful downloads count: ${downloadSuccessCount}`);

  if (downloadSuccessCount % 2 === 0) {
    console.log('[ALLVID Ads] Milestoned 2 downloads! Displaying Interstitial Ad.');
    await showInterstitial();
  }
}

/**
 * 3. Rewarded Ad:
 * Watch ad to unlock HD 1080p, 1440p, or 4K download
 */
export async function showRewarded(): Promise<boolean> {
  console.log('[ALLVID Ads] Triggering Rewarded Ad...');

  if (Capacitor.isNativePlatform()) {
    try {
      const AdMob = await getAdMobPlugin();
      if (AdMob) {
        return new Promise(async (resolve) => {
          let rewardGranted = false;

          const rewardListener = await AdMob.addListener('onRewarded', () => {
            rewardGranted = true;
          });

          const dismissListener = await AdMob.addListener('onRewardedVideoAdDismissed', () => {
            rewardListener.remove();
            dismissListener.remove();
            resolve(rewardGranted);
          });

          await AdMob.prepareRewardVideoAd({
            adId: ADMOB_CONFIG.REWARDED_ID,
            isTesting: ADMOB_CONFIG.IS_TESTING,
          });

          await AdMob.showRewardVideoAd();
        });
      }
    } catch (err) {
      console.warn('[ALLVID Ads] Rewarded ad failed on native:', err);
      return false;
    }
  }

  return true;
}

/**
 * 4. App Open Ad:
 * Shown on app launch for APK version
 */
export async function showAppOpen(): Promise<void> {
  if (!Capacitor.isNativePlatform()) return;
  console.log('[ALLVID Ads] Triggering App Open Ad for APK launch...');
  try {
    const AdMob = await getAdMobPlugin();
    if (AdMob) {
      // If plugin supports app open or via interstitial fallback:
      await AdMob.prepareInterstitial({
        adId: ADMOB_CONFIG.APP_OPEN_ID,
        isTesting: ADMOB_CONFIG.IS_TESTING,
      });
      await AdMob.showInterstitial();
    }
  } catch (err) {
    console.log('[ALLVID Ads] App open ad skipped or unavailable');
  }
}

/**
 * Check if a quality is currently unlocked (within 24h)
 */
export function isQualityUnlocked(quality: QualityType, isVipActive = false): boolean {
  if (isVipActive) return true;
  if (quality === 'mp3' || quality === 'jpg') return true;

  if (typeof window === 'undefined' || !window.localStorage) return false;

  const key = `${STORAGE_PREFIX}unlocked_${quality.toLowerCase()}`;
  const raw = window.localStorage.getItem(key);
  if (!raw) return false;

  const timestamp = parseInt(raw, 10);
  if (isNaN(timestamp)) return false;

  const now = Date.now();
  if (now - timestamp < UNLOCK_DURATION_MS) {
    return true;
  }

  // Expired
  window.localStorage.removeItem(key);
  return false;
}

/**
 * Get remaining 720p free downloads today (max 3)
 */
export function getRemaining720pToday(): number {
  if (typeof window === 'undefined' || !window.localStorage) return MAX_FREE_720P_PER_DAY;

  const todayStr = new Date().toISOString().slice(0, 10);
  const savedDate = window.localStorage.getItem(`${STORAGE_PREFIX}720p_date`);
  if (savedDate !== todayStr) {
    return MAX_FREE_720P_PER_DAY;
  }

  const countStr = window.localStorage.getItem(`${STORAGE_PREFIX}720p_count`);
  const count = countStr ? parseInt(countStr, 10) : 0;
  return Math.max(0, MAX_FREE_720P_PER_DAY - count);
}

/**
 * Record a free 720p download
 */
export function consumeFree720p(): boolean {
  if (typeof window === 'undefined' || !window.localStorage) return true;

  const todayStr = new Date().toISOString().slice(0, 10);
  const savedDate = window.localStorage.getItem(`${STORAGE_PREFIX}720p_date`);
  let count = 0;

  if (savedDate === todayStr) {
    count = parseInt(window.localStorage.getItem(`${STORAGE_PREFIX}720p_count`) || '0', 10);
  } else {
    window.localStorage.setItem(`${STORAGE_PREFIX}720p_date`, todayStr);
  }

  if (count >= MAX_FREE_720P_PER_DAY) {
    return false;
  }

  window.localStorage.setItem(`${STORAGE_PREFIX}720p_count`, String(count + 1));
  return true;
}

/**
 * Save unlock timestamp (24h validity)
 */
export function saveQualityUnlocked(quality: QualityType): void {
  if (typeof window === 'undefined' || !window.localStorage) return;
  const key = `${STORAGE_PREFIX}unlocked_${quality.toLowerCase()}`;
  window.localStorage.setItem(key, String(Date.now()));
}

/**
 * Formatted time remaining for 24h unlock
 */
export function getUnlockRemainingTime(quality: QualityType): string | null {
  if (typeof window === 'undefined' || !window.localStorage) return null;
  const key = `${STORAGE_PREFIX}unlocked_${quality.toLowerCase()}`;
  const raw = window.localStorage.getItem(key);
  if (!raw) return null;

  const timestamp = parseInt(raw, 10);
  const remainingMs = UNLOCK_DURATION_MS - (Date.now() - timestamp);
  if (remainingMs <= 0) return null;

  const hours = Math.floor(remainingMs / (1000 * 60 * 60));
  const minutes = Math.floor((remainingMs % (1000 * 60 * 60)) / (1000 * 60));
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`;
}

/**
 * Unlock Quality Gateway
 * - 720p: FREE (up to 3x/day, then 1 ad for 24h pass)
 * - 1080p: 1 Rewarded Ad (24h pass)
 * - 1440p / 4K: Interstitial + Rewarded (24h pass)
 * - In Web: 5s countdown modal with Google AdSense slot
 * - In APK: Real AdMob Rewarded ID / Interstitial
 */
export async function unlockQuality(
  quality: QualityType,
  options: {
    isVipActive?: boolean;
    onStartAd?: () => void;
    onAdCountDown?: (secondsLeft: number) => void;
    onSuccess: () => void;
    onError?: (error: string) => void;
  }
): Promise<void> {
  const { isVipActive = false, onSuccess, onError, onStartAd, onAdCountDown } = options;

  // 1. VIP bypass
  if (isVipActive) {
    onSuccess();
    return;
  }

  // 2. Audio or Cover is always free
  if (quality === 'mp3' || quality === 'jpg') {
    onSuccess();
    return;
  }

  // 3. Already unlocked within 24h
  if (isQualityUnlocked(quality, false)) {
    onSuccess();
    return;
  }

  // 4. 720p Free check (3x per day limit)
  if (quality === '720p') {
    const remaining = getRemaining720pToday();
    if (remaining > 0) {
      consumeFree720p();
      onSuccess();
      return;
    }
  }

  // 5. APK / Native Platform: Show Real AdMob
  if (Capacitor.isNativePlatform()) {
    try {
      onStartAd?.();

      if (quality === '1440p' || quality === '4K') {
        // Interstitial + Rewarded
        console.log('[ALLVID Ads] Unlocking Ultra HD: Interstitial first...');
        await showInterstitial();
      }

      console.log('[ALLVID Ads] Showing Rewarded Ad for unlock...');
      const rewarded = await showRewarded();
      if (rewarded !== false) {
        saveQualityUnlocked(quality);
        onSuccess();
      } else {
        onError?.('Reward was not completed. Quality remains locked.');
      }
      return;
    } catch (err: any) {
      console.warn('[ALLVID Ads] Native ad error, falling back to simulated counter:', err);
    }
  }

  // 6. Web Platform: Show 5-second countdown modal with AdSense slot
  let seconds = 5;
  onStartAd?.();
  onAdCountDown?.(seconds);

  const timer = setInterval(() => {
    seconds -= 1;
    onAdCountDown?.(seconds);
    if (seconds <= 0) {
      clearInterval(timer);
      saveQualityUnlocked(quality);
      onSuccess();
    }
  }, 1000);
}
