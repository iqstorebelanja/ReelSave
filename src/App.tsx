import React, { useState, useEffect } from 'react';
import {
  initAds,
  unlockQuality,
  isQualityUnlocked,
  getRemaining720pToday,
  getUnlockRemainingTime,
  recordSuccessfulDownload,
  QualityType,
  ADMOB_CONFIG
} from './utils/ads';
import QualityButton from './components/QualityButton';
import { Capacitor } from '@capacitor/core';
import { Filesystem, Directory } from '@capacitor/filesystem';

interface VideoMedia {
  id: string;
  url: string;
  platform: 'tiktok' | 'instagram' | 'youtube' | 'twitter' | 'facebook' | 'threads' | 'capcut' | 'generic';
  title: string;
  author: string;
  thumbnail: string;
  previewUrl: string;
  duration: string;
}

interface DownloadHistoryItem {
  id: string;
  title: string;
  platform: string;
  quality: QualityType;
  date: string;
  fileName: string;
  url: string;
}

export function App() {
  const [inputUrl, setInputUrl] = useState('');
  const [isBatchMode, setIsBatchMode] = useState(false);
  const [batchUrls, setBatchUrls] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [media, setMedia] = useState<VideoMedia | null>(null);
  const [selectedQuality, setSelectedQuality] = useState<QualityType>('1080p');
  const [isDownloading, setIsDownloading] = useState(false);
  const [downloadProgress, setDownloadProgress] = useState<number | null>(null);
  const [downloadHistory, setDownloadHistory] = useState<DownloadHistoryItem[]>([]);
  const [isVipActive, setIsVipActive] = useState(false);
  const [showVipModal, setShowVipModal] = useState(false);
  const [showPreviewModal, setShowPreviewModal] = useState(false);
  const [showHistoryModal, setShowHistoryModal] = useState(false);
  const [activeTab, setActiveTab] = useState<'single' | 'batch' | 'history'>('single');

  // Ad simulation modal states (Web countdown modal)
  const [isAdModalOpen, setIsAdModalOpen] = useState(false);
  const [adCountdown, setAdCountdown] = useState(5);
  const [adQualityTarget, setAdQualityTarget] = useState<QualityType>('1080p');

  // Trigger AdMob / AdSense initialization on mount
  useEffect(() => {
    initAds();

    // Load saved VIP status and download history
    const savedVip = localStorage.getItem('allvid_vip_active');
    if (savedVip === 'true') {
      setIsVipActive(true);
    }

    const savedHistory = localStorage.getItem('allvid_download_history');
    if (savedHistory) {
      try {
        setDownloadHistory(JSON.parse(savedHistory));
      } catch (e) {
        console.error('Failed to parse history', e);
      }
    }
  }, []);

  const toggleVip = () => {
    const nextState = !isVipActive;
    setIsVipActive(nextState);
    localStorage.setItem('allvid_vip_active', String(nextState));
    if (nextState) {
      setShowVipModal(false);
    }
  };

  const detectPlatform = (url: string): VideoMedia['platform'] => {
    const lower = url.toLowerCase();
    if (lower.includes('tiktok.com')) return 'tiktok';
    if (lower.includes('instagram.com')) return 'instagram';
    if (lower.includes('youtube.com') || lower.includes('youtu.be')) return 'youtube';
    if (lower.includes('twitter.com') || lower.includes('x.com')) return 'twitter';
    if (lower.includes('facebook.com') || lower.includes('fb.watch')) return 'facebook';
    if (lower.includes('threads.net')) return 'threads';
    if (lower.includes('capcut.com')) return 'capcut';
    return 'generic';
  };

  const handleSmartPaste = async () => {
    try {
      if (navigator.clipboard) {
        const text = await navigator.clipboard.readText();
        if (text) {
          setInputUrl(text.trim());
          handleExtract(text.trim());
        }
      }
    } catch (err) {
      console.warn('Clipboard read permission denied', err);
    }
  };

  const handleExtract = (targetUrl?: string) => {
    const urlToProcess = targetUrl || inputUrl;
    if (!urlToProcess.trim()) return;

    setIsLoading(true);
    const platform = detectPlatform(urlToProcess);

    // Realistic multi-platform media generator
    setTimeout(() => {
      const mockResult: VideoMedia = {
        id: 'vid_' + Date.now(),
        url: urlToProcess,
        platform,
        title: getPlatformTitle(platform, urlToProcess),
        author: getPlatformAuthor(platform),
        thumbnail: getPlatformThumb(platform),
        previewUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
        duration: '00:45',
      };
      setMedia(mockResult);
      setIsLoading(false);
    }, 700);
  };

  const getPlatformTitle = (platform: VideoMedia['platform'], url: string) => {
    switch (platform) {
      case 'tiktok': return 'TikTok Viral Trend [No Watermark HD]';
      case 'instagram': return 'Instagram Reels Ultra HD Clip';
      case 'youtube': return 'YouTube Shorts / 4K Video Extract';
      case 'twitter': return 'X/Twitter Media Clip';
      case 'facebook': return 'Facebook Watch HD Video';
      case 'threads': return 'Threads Dynamic Post Video';
      case 'capcut': return 'CapCut Template Video Without Watermark';
      default: return 'Online Video Stream Streamlined';
    }
  };

  const getPlatformAuthor = (platform: VideoMedia['platform']) => {
    switch (platform) {
      case 'tiktok': return '@creator.tok';
      case 'instagram': return '@reels.creator';
      case 'youtube': return 'Shorts Channel';
      case 'twitter': return '@x_creator';
      case 'facebook': return 'FB Video Creator';
      case 'threads': return '@threads_user';
      case 'capcut': return '@capcut_template';
      default: return 'Online Creator';
    }
  };

  const getPlatformThumb = (platform: VideoMedia['platform']) => {
    return 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=600&q=80';
  };

  const handleQualityClick = (quality: QualityType) => {
    setSelectedQuality(quality);

    // Call unlockQuality from ads.ts
    unlockQuality(quality, {
      isVipActive,
      onStartAd: () => {
        setAdQualityTarget(quality);
        setIsAdModalOpen(true);
        setAdCountdown(5);
      },
      onAdCountDown: (sec) => {
        setAdCountdown(sec);
      },
      onSuccess: () => {
        setIsAdModalOpen(false);
        triggerDownload(quality);
      },
      onError: (err) => {
        setIsAdModalOpen(false);
        alert(err || 'Failed to unlock quality');
      }
    });
  };

  const triggerDownload = async (quality: QualityType) => {
    if (!media) return;
    setIsDownloading(true);
    setDownloadProgress(10);

    const progressInterval = setInterval(() => {
      setDownloadProgress((prev) => {
        if (!prev) return 20;
        if (prev >= 90) {
          clearInterval(progressInterval);
          return 90;
        }
        return prev + 20;
      });
    }, 250);

    setTimeout(async () => {
      clearInterval(progressInterval);
      setDownloadProgress(100);

      const fileName = `ALLVID_${media.platform}_${quality}_${Date.now()}.${quality === 'mp3' ? 'mp3' : quality === 'jpg' ? 'jpg' : 'mp4'}`;

      // Save to Native APK Downloads folder via @capacitor/filesystem if native
      if (Capacitor.isNativePlatform()) {
        try {
          await Filesystem.writeFile({
            path: `Download/${fileName}`,
            data: 'QUxMVklEIFZpZGVvIFN0cmVhbSBDb250ZW50', // Mock binary base64
            directory: Directory.ExternalStorage,
          });
          console.log('[ALLVID] Saved to Android Downloads folder');
        } catch (e) {
          console.warn('[ALLVID] Native filesystem write fallback:', e);
        }
      } else {
        // Web fallback: simulate direct browser file download
        const a = document.createElement('a');
        a.href = media.previewUrl;
        a.download = fileName;
        a.target = '_blank';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      }

      // Record successful download & trigger AdMob Interstitial after every 2 downloads
      await recordSuccessfulDownload(isVipActive);

      // Save to history
      const newHistoryItem: DownloadHistoryItem = {
        id: 'dl_' + Date.now(),
        title: media.title,
        platform: media.platform.toUpperCase(),
        quality,
        date: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        fileName,
        url: media.url,
      };

      const updatedHistory = [newHistoryItem, ...downloadHistory.slice(0, 20)];
      setDownloadHistory(updatedHistory);
      localStorage.setItem('allvid_download_history', JSON.stringify(updatedHistory));

      setIsDownloading(false);
      setDownloadProgress(null);
    }, 1500);
  };

  const handleBatchProcess = () => {
    const urls = batchUrls
      .split('\n')
      .map((u) => u.trim())
      .filter((u) => u.length > 5)
      .slice(0, 10);

    if (urls.length === 0) return;

    setIsLoading(true);
    setTimeout(() => {
      handleExtract(urls[0]);
      setIsLoading(false);
      setIsBatchMode(false);
    }, 1000);
  };

  const remaining720p = getRemaining720pToday();

  return (
    <div className="min-h-screen bg-[#090A0F] text-slate-100 flex flex-col font-sans pb-28">
      {/* Top Navbar */}
      <header className="sticky top-0 z-40 bg-[#090A0F]/80 backdrop-blur-xl border-b border-white/[0.08] px-4 py-3.5 flex items-center justify-between">
        <div className="flex items-center space-x-2.5">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-[#00D1FF] to-[#0066FF] flex items-center justify-center shadow-neon font-black text-black text-lg tracking-wider">
            AV
          </div>
          <div>
            <div className="flex items-center space-x-1.5">
              <span className="font-extrabold text-white text-lg tracking-tight">ALLVID</span>
              <span className="text-[10px] bg-[#00D1FF]/20 text-[#00D1FF] border border-[#00D1FF]/30 px-1.5 py-0.2 rounded font-bold uppercase">
                PRO 4K
              </span>
            </div>
            <p className="text-[10px] text-white/50 -mt-0.5">All-in-One Fast Downloader</p>
          </div>
        </div>

        <div className="flex items-center space-x-2">
          <button
            onClick={() => setShowVipModal(true)}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center space-x-1.5 border ${
              isVipActive
                ? 'bg-[#FFD700]/15 text-[#FFD700] border-[#FFD700]/40 shadow-gold'
                : 'bg-white/[0.05] hover:bg-white/[0.1] text-white/80 border-white/10'
            }`}
          >
            <span>{isVipActive ? '👑 VIP ACTIVE' : '⚡ GET VIP'}</span>
          </button>
        </div>
      </header>

      {/* Main Container */}
      <main className="flex-1 max-w-lg w-full mx-auto px-4 pt-4 space-y-4">
        {/* Mode Selector */}
        <div className="flex bg-[#12141C] p-1 rounded-2xl border border-white/[0.08]">
          <button
            onClick={() => { setActiveTab('single'); setIsBatchMode(false); }}
            className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all ${
              activeTab === 'single'
                ? 'bg-[#00D1FF] text-black shadow-neon'
                : 'text-white/60 hover:text-white'
            }`}
          >
            Direct Link
          </button>
          <button
            onClick={() => { setActiveTab('batch'); setIsBatchMode(true); }}
            className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all ${
              activeTab === 'batch'
                ? 'bg-[#00D1FF] text-black shadow-neon'
                : 'text-white/60 hover:text-white'
            }`}
          >
            Batch 10 URLs
          </button>
          <button
            onClick={() => { setActiveTab('history'); }}
            className={`flex-1 py-2 text-xs font-bold rounded-xl transition-all ${
              activeTab === 'history'
                ? 'bg-[#00D1FF] text-black shadow-neon'
                : 'text-white/60 hover:text-white'
            }`}
          >
            History ({downloadHistory.length})
          </button>
        </div>

        {/* Tab 1: Single URL Smart Extraction */}
        {activeTab === 'single' && (
          <div className="space-y-4">
            {/* Input Card */}
            <div className="bg-[#12141C]/90 rounded-3xl p-4 border border-white/[0.08] shadow-2xl relative overflow-hidden backdrop-blur-xl">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-white/70 tracking-wide uppercase">
                  Paste Media Link
                </span>
                <span className="text-[11px] text-[#00D1FF] font-medium flex items-center space-x-1">
                  <span>Auto-detect platform</span>
                </span>
              </div>

              <div className="relative">
                <input
                  type="text"
                  value={inputUrl}
                  onChange={(e) => setInputUrl(e.target.value)}
                  placeholder="https://www.tiktok.com/@... or instagram.com/reel/..."
                  className="w-full bg-[#090A0F] border border-white/[0.12] focus:border-[#00D1FF] text-sm text-white px-3.5 py-3.5 rounded-2xl outline-none transition-all placeholder:text-white/30 pr-24"
                />
                <button
                  onClick={handleSmartPaste}
                  className="absolute right-2 top-2 bottom-2 px-3 bg-white/[0.08] hover:bg-white/[0.15] text-[#00D1FF] rounded-xl text-xs font-bold transition-all flex items-center space-x-1 border border-[#00D1FF]/20"
                >
                  <span>📋 Paste</span>
                </button>
              </div>

              <button
                onClick={() => handleExtract()}
                disabled={isLoading || !inputUrl.trim()}
                className="w-full mt-3 py-3.5 rounded-2xl font-black text-black bg-gradient-to-r from-[#00D1FF] to-[#0099FF] hover:brightness-110 active:scale-[0.99] transition-all shadow-neon disabled:opacity-50 text-sm flex items-center justify-center space-x-2"
              >
                {isLoading ? (
                  <span className="inline-block animate-spin mr-2">⟳</span>
                ) : (
                  <span>⚡ EXTRACT & ANALYZE</span>
                )}
              </button>

              {/* Supported Platform Badges */}
              <div className="mt-4 pt-3 border-t border-white/[0.06] flex items-center justify-between overflow-x-auto text-[11px] text-white/50 space-x-2">
                <span className="bg-white/5 px-2 py-1 rounded-lg">TikTok No-WM</span>
                <span className="bg-white/5 px-2 py-1 rounded-lg">IG Reels</span>
                <span className="bg-white/5 px-2 py-1 rounded-lg">Shorts</span>
                <span className="bg-white/5 px-2 py-1 rounded-lg">X/Twitter</span>
                <span className="bg-white/5 px-2 py-1 rounded-lg">CapCut</span>
              </div>
            </div>

            {/* Extracted Media Result */}
            {media && (
              <div className="bg-[#12141C]/90 rounded-3xl p-4 border border-white/[0.08] shadow-2xl space-y-4 animate-fade-in">
                {/* Media Header */}
                <div className="flex space-x-3.5">
                  <div className="relative w-24 h-24 rounded-2xl overflow-hidden bg-black/40 border border-white/10 flex-shrink-0">
                    <img
                      src={media.thumbnail}
                      alt={media.title}
                      className="w-full h-full object-cover"
                    />
                    <div className="absolute inset-0 bg-black/20 flex items-center justify-center">
                      <button
                        onClick={() => setShowPreviewModal(true)}
                        className="w-8 h-8 rounded-full bg-[#00D1FF] text-black flex items-center justify-center shadow-neon font-bold text-xs hover:scale-110 transition-transform"
                        title="Watch Preview"
                      >
                        ▶
                      </button>
                    </div>
                  </div>

                  <div className="flex-1 min-w-0 flex flex-col justify-between py-0.5">
                    <div>
                      <div className="flex items-center space-x-2 mb-1">
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-[#00D1FF]/20 text-[#00D1FF] border border-[#00D1FF]/30 uppercase">
                          {media.platform}
                        </span>
                        <span className="text-xs text-white/40">{media.duration}</span>
                      </div>
                      <h3 className="text-sm font-bold text-white truncate">{media.title}</h3>
                      <p className="text-xs text-white/50">{media.author}</p>
                    </div>

                    <button
                      onClick={() => setShowPreviewModal(true)}
                      className="text-xs text-[#00D1FF] font-semibold hover:underline text-left flex items-center space-x-1"
                    >
                      <span>▶ Preview Video Player</span>
                    </button>
                  </div>
                </div>

                {/* Progress bar during download */}
                {isDownloading && downloadProgress !== null && (
                  <div className="bg-[#090A0F] p-3 rounded-2xl border border-[#00D1FF]/30 space-y-1.5">
                    <div className="flex justify-between text-xs font-bold">
                      <span className="text-[#00D1FF]">Downloading {selectedQuality}...</span>
                      <span className="text-white">{downloadProgress}%</span>
                    </div>
                    <div className="w-full bg-white/10 h-2 rounded-full overflow-hidden">
                      <div
                        className="bg-[#00D1FF] h-full transition-all duration-200"
                        style={{ width: `${downloadProgress}%` }}
                      />
                    </div>
                  </div>
                )}

                {/* Quality Selection Grid using QualityButton */}
                <div className="space-y-2">
                  <div className="flex items-center justify-between text-xs text-white/60 px-1 font-semibold">
                    <span>SELECT QUALITY & FORMAT</span>
                    <span>{isVipActive ? '👑 ALL UNLOCKED' : 'AdMob Gate Active'}</span>
                  </div>

                  {/* 4K Ultra HD */}
                  <QualityButton
                    quality="4K"
                    title="4K Ultra HD (2160p)"
                    subtitle={getUnlockRemainingTime('4K') ? `Unlocked: ${getUnlockRemainingTime('4K')} left` : 'Crystal clear high-bitrate video'}
                    badgeText={isVipActive ? 'VIP FREE' : isQualityUnlocked('4K') ? 'UNLOCKED (24h)' : '⚡ INTERSTITIAL + REWARDED'}
                    isUnlocked={isQualityUnlocked('4K', isVipActive)}
                    isSelected={selectedQuality === '4K'}
                    isDownloading={isDownloading}
                    isVipActive={isVipActive}
                    onClick={() => handleQualityClick('4K')}
                  />

                  {/* 1440p 2K */}
                  <QualityButton
                    quality="1440p"
                    title="1440p 2K QHD"
                    subtitle={getUnlockRemainingTime('1440p') ? `Unlocked: ${getUnlockRemainingTime('1440p')} left` : 'High definition enhanced audio'}
                    badgeText={isVipActive ? 'VIP FREE' : isQualityUnlocked('1440p') ? 'UNLOCKED (24h)' : '⚡ INTERSTITIAL + REWARDED'}
                    isUnlocked={isQualityUnlocked('1440p', isVipActive)}
                    isSelected={selectedQuality === '1440p'}
                    isDownloading={isDownloading}
                    isVipActive={isVipActive}
                    onClick={() => handleQualityClick('1440p')}
                  />

                  {/* 1080p Full HD */}
                  <QualityButton
                    quality="1080p"
                    title="1080p Full HD"
                    subtitle={getUnlockRemainingTime('1080p') ? `Unlocked: ${getUnlockRemainingTime('1080p')} left` : 'Standard crisp resolution'}
                    badgeText={isVipActive ? 'VIP FREE' : isQualityUnlocked('1080p') ? 'UNLOCKED (24h)' : '🎬 1 REWARDED AD'}
                    isUnlocked={isQualityUnlocked('1080p', isVipActive)}
                    isSelected={selectedQuality === '1080p'}
                    isDownloading={isDownloading}
                    isVipActive={isVipActive}
                    onClick={() => handleQualityClick('1080p')}
                  />

                  {/* 720p HD Free tier with 3x daily limit */}
                  <QualityButton
                    quality="720p"
                    title="720p Fast Download"
                    subtitle={
                      remaining720p > 0
                        ? `${remaining720p} of 3 free downloads left today`
                        : 'Daily limit reached (Watch 1 ad for 24h pass)'
                    }
                    badgeText={remaining720p > 0 ? `FREE (${remaining720p}/3)` : '🎬 WATCH AD'}
                    isUnlocked={remaining720p > 0 || isQualityUnlocked('720p', isVipActive)}
                    isSelected={selectedQuality === '720p'}
                    isDownloading={isDownloading}
                    isVipActive={isVipActive}
                    onClick={() => handleQualityClick('720p')}
                  />

                  {/* MP3 Audio */}
                  <QualityButton
                    quality="mp3"
                    title="Audio MP3 (320kbps)"
                    subtitle="Direct clean audio extract"
                    badgeText="100% FREE"
                    isUnlocked={true}
                    isSelected={selectedQuality === 'mp3'}
                    isDownloading={isDownloading}
                    isVipActive={isVipActive}
                    onClick={() => handleQualityClick('mp3')}
                  />

                  {/* JPG Cover */}
                  <QualityButton
                    quality="jpg"
                    title="Thumbnail Cover (JPG)"
                    subtitle="Original highest resolution photo"
                    badgeText="100% FREE"
                    isUnlocked={true}
                    isSelected={selectedQuality === 'jpg'}
                    isDownloading={isDownloading}
                    isVipActive={isVipActive}
                    onClick={() => handleQualityClick('jpg')}
                  />
                </div>
              </div>
            )}
          </div>
        )}

        {/* Tab 2: Batch 10 URLs Mode */}
        {activeTab === 'batch' && (
          <div className="bg-[#12141C]/90 rounded-3xl p-5 border border-white/[0.08] shadow-2xl space-y-4">
            <div>
              <h2 className="text-base font-bold text-white">Batch Downloader (Up to 10 URLs)</h2>
              <p className="text-xs text-white/50 mt-1">
                Paste up to 10 video links (one URL per line). The engine will queue and extract them simultaneously.
              </p>
            </div>

            <textarea
              rows={6}
              value={batchUrls}
              onChange={(e) => setBatchUrls(e.target.value)}
              placeholder="https://tiktok.com/@...&#10;https://instagram.com/reel/...&#10;https://youtube.com/shorts/..."
              className="w-full bg-[#090A0F] border border-white/[0.12] focus:border-[#00D1FF] rounded-2xl p-3 text-xs text-white outline-none font-mono placeholder:text-white/30"
            />

            <div className="flex items-center justify-between text-xs text-white/50">
              <span>Limit: 10 URLs per queue</span>
              <span>{batchUrls.split('\n').filter((u) => u.trim().length > 5).length} detected</span>
            </div>

            <button
              onClick={handleBatchProcess}
              disabled={isLoading || !batchUrls.trim()}
              className="w-full py-3.5 rounded-2xl font-black text-black bg-[#00D1FF] hover:bg-[#00B8E6] transition-all shadow-neon disabled:opacity-50 text-sm"
            >
              PROCESS BATCH DOWNLOAD
            </button>
          </div>
        )}

        {/* Tab 3: Download History */}
        {activeTab === 'history' && (
          <div className="bg-[#12141C]/90 rounded-3xl p-5 border border-white/[0.08] shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-base font-bold text-white">Download Vault</h2>
                <p className="text-xs text-white/50">Stored locally in your device downloads</p>
              </div>
              {downloadHistory.length > 0 && (
                <button
                  onClick={() => {
                    setDownloadHistory([]);
                    localStorage.removeItem('allvid_download_history');
                  }}
                  className="text-xs text-red-400 hover:underline"
                >
                  Clear All
                </button>
              )}
            </div>

            {downloadHistory.length === 0 ? (
              <div className="text-center py-12 text-white/40 text-xs">
                No recent downloads yet. Paste a link above to get started!
              </div>
            ) : (
              <div className="space-y-2.5">
                {downloadHistory.map((item) => (
                  <div
                    key={item.id}
                    className="p-3.5 bg-white/[0.02] border border-white/[0.06] rounded-2xl flex items-center justify-between"
                  >
                    <div className="min-w-0 pr-3">
                      <div className="flex items-center space-x-2">
                        <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-[#00D1FF]/20 text-[#00D1FF]">
                          {item.platform}
                        </span>
                        <span className="text-[10px] text-white/40">{item.quality}</span>
                        <span className="text-[10px] text-white/30">• {item.date}</span>
                      </div>
                      <div className="text-xs font-semibold text-white truncate mt-1">
                        {item.title}
                      </div>
                      <div className="text-[10px] text-white/40 font-mono truncate">{item.fileName}</div>
                    </div>

                    <button
                      onClick={() => {
                        setInputUrl(item.url);
                        setActiveTab('single');
                        handleExtract(item.url);
                      }}
                      className="px-3 py-1.5 bg-[#00D1FF]/10 text-[#00D1FF] border border-[#00D1FF]/30 rounded-xl text-xs font-bold hover:bg-[#00D1FF] hover:text-black transition-all flex-shrink-0"
                    >
                      Re-fetch
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>

      {/* Web 5-Second Countdown Modal with AdSense Placeholder */}
      {isAdModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="bg-[#12141C] border border-[#00D1FF]/40 rounded-3xl p-6 max-w-sm w-full shadow-neon text-center space-y-4 relative">
            <div className="w-14 h-14 rounded-2xl bg-[#00D1FF]/10 border border-[#00D1FF]/40 text-[#00D1FF] flex items-center justify-center mx-auto text-2xl font-black">
              {adCountdown}
            </div>

            <div>
              <h3 className="text-lg font-black text-white">Unlocking {adQualityTarget} HD Pass</h3>
              <p className="text-xs text-white/60 mt-1">
                Please wait {adCountdown} seconds while your HD download token is generated.
              </p>
            </div>

            {/* Simulated Google AdSense Slot */}
            <div className="w-full h-36 bg-[#090A0F] border border-dashed border-white/20 rounded-2xl flex flex-col items-center justify-center p-3 relative overflow-hidden">
              <span className="text-[10px] uppercase tracking-wider text-white/30 absolute top-2 right-2">
                Advertisement
              </span>
              <div className="text-xs text-[#00D1FF] font-semibold">Google AdSense Partner Slot</div>
              <p className="text-[11px] text-white/40 mt-1 text-center max-w-[200px]">
                Support ALLVID development by viewing sponsored partner announcements.
              </p>
            </div>

            <div className="text-[11px] text-white/40">
              Unlocks full 24-hour unlimited high bitrate pass for this quality.
            </div>
          </div>
        </div>
      )}

      {/* Preview Player Modal */}
      {showPreviewModal && media && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md">
          <div className="bg-[#12141C] border border-white/10 rounded-3xl max-w-md w-full overflow-hidden shadow-2xl">
            <div className="p-4 border-b border-white/10 flex items-center justify-between">
              <div className="text-sm font-bold text-white truncate pr-4">{media.title}</div>
              <button
                onClick={() => setShowPreviewModal(false)}
                className="w-8 h-8 rounded-full bg-white/10 text-white flex items-center justify-center hover:bg-white/20 font-bold"
              >
                ✕
              </button>
            </div>

            <div className="bg-black aspect-video relative flex items-center justify-center">
              <video
                src={media.previewUrl}
                controls
                autoPlay
                className="w-full h-full object-contain"
              />
            </div>

            <div className="p-4 flex items-center justify-between">
              <span className="text-xs text-white/50">{media.author} • {media.duration}</span>
              <button
                onClick={() => {
                  setShowPreviewModal(false);
                  handleQualityClick('1080p');
                }}
                className="px-4 py-2 bg-[#00D1FF] text-black font-bold text-xs rounded-xl shadow-neon"
              >
                Download 1080p
              </button>
            </div>
          </div>
        </div>
      )}

      {/* VIP Modal */}
      {showVipModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md">
          <div className="bg-[#12141C] border border-[#FFD700]/40 rounded-3xl max-w-sm w-full p-6 shadow-gold text-center space-y-4">
            <div className="w-16 h-16 rounded-2xl bg-[#FFD700]/10 border border-[#FFD700]/40 text-[#FFD700] text-3xl flex items-center justify-center mx-auto shadow-gold">
              👑
            </div>

            <div>
              <h3 className="text-xl font-black text-white">ALLVID VIP Pass</h3>
              <p className="text-xs text-white/60 mt-1">
                Zero Ads, Instant 4K Downloads, Unlimited Daily 720p & 1080p with no waiting.
              </p>
            </div>

            <div className="space-y-2 text-left bg-white/[0.03] p-4 rounded-2xl border border-white/[0.08] text-xs">
              <div className="flex items-center space-x-2 text-white">
                <span className="text-[#00FF88]">✓</span>
                <span>Permanent ad removal (No banners or interstitials)</span>
              </div>
              <div className="flex items-center space-x-2 text-white">
                <span className="text-[#00FF88]">✓</span>
                <span>Unrestricted 4K 60fps & 1440p download access</span>
              </div>
              <div className="flex items-center space-x-2 text-white">
                <span className="text-[#00FF88]">✓</span>
                <span>Unlimited batch queue downloads</span>
              </div>
            </div>

            <button
              onClick={toggleVip}
              className="w-full py-3.5 rounded-2xl font-black text-black bg-[#FFD700] hover:brightness-110 shadow-gold text-sm transition-all"
            >
              {isVipActive ? 'CANCEL VIP MEMBERSHIP' : 'ACTIVATE VIP PASS ($29/mo)'}
            </button>

            <button
              onClick={() => setShowVipModal(false)}
              className="text-xs text-white/40 hover:text-white"
            >
              Dismiss
            </button>
          </div>
        </div>
      )}

      {/* Sticky Bottom Monetization Banner: AdMob Banner on Native, AdSense slot on Web */}
      <div className="fixed bottom-0 inset-x-0 z-30 bg-[#090A0F]/95 border-t border-white/[0.08] p-2 backdrop-blur-xl flex flex-col items-center justify-center min-h-[56px]">
        {isVipActive ? (
          <div className="text-xs text-[#FFD700] font-bold flex items-center space-x-1.5">
            <span>👑 VIP Active: Ads Disabled</span>
          </div>
        ) : Capacitor.isNativePlatform() ? (
          <div className="text-center">
            <span className="text-[10px] text-white/40 font-mono">
              [Native AdMob Sticky Banner Active • Unit: {ADMOB_CONFIG.BANNER_ID.slice(0, 18)}...]
            </span>
          </div>
        ) : (
          <div className="w-full max-w-md h-12 bg-white/[0.02] border border-dashed border-white/15 rounded-xl flex items-center justify-between px-4">
            <span className="text-[10px] text-white/30 uppercase tracking-widest font-mono">AdSense Banner</span>
            <span className="text-xs text-[#00D1FF] font-medium">Download Faster with ALLVID</span>
            <span className="text-[10px] bg-white/10 px-2 py-0.5 rounded text-white/40">320x50</span>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
