import React from 'react';
import { QualityType } from '../utils/ads';

export interface QualityButtonProps {
  quality: QualityType;
  title: string;
  subtitle: string;
  badgeText: string;
  isUnlocked: boolean;
  isSelected?: boolean;
  isDownloading?: boolean;
  isVipActive?: boolean;
  onClick: () => void;
}

export const QualityButton: React.FC<QualityButtonProps> = ({
  quality,
  title,
  subtitle,
  badgeText,
  isUnlocked,
  isSelected = false,
  isDownloading = false,
  isVipActive = false,
  onClick,
}) => {
  return (
    <button
      onClick={onClick}
      disabled={isDownloading}
      className={`w-full flex items-center justify-between p-3.5 rounded-2xl transition-all duration-200 border ${
        isSelected
          ? 'border-[#00D1FF] bg-[#00D1FF]/10'
          : isUnlocked
          ? 'border-white/10 bg-white/[0.04]'
          : 'border-white/[0.06] bg-white/[0.02]'
      }`}
    >
      <div className="flex items-center space-x-3">
        <div
          className={`w-9 h-9 rounded-xl flex items-center justify-center border text-xs font-bold ${
            isVipActive
              ? 'border-[#FFD700]/40 text-[#FFD700] bg-[#FFD700]/10'
              : isUnlocked
              ? 'border-[#00D1FF]/40 text-[#00D1FF] bg-[#00D1FF]/10'
              : 'border-white/10 text-white/50 bg-white/5'
          }`}
        >
          {isVipActive ? 'VIP' : isUnlocked ? '✓' : '🔒'}
        </div>
        <div className="text-left">
          <div className="text-sm font-semibold text-white flex items-center space-x-1.5">
            <span>{title}</span>
            {isVipActive && (
              <span className="text-[10px] bg-[#FFD700]/20 text-[#FFD700] px-1.5 py-0.5 rounded font-bold">
                VIP
              </span>
            )}
          </div>
          <div className="text-xs text-white/50">{subtitle}</div>
        </div>
      </div>
      <div
        className={`text-xs px-2.5 py-1 rounded-lg border font-semibold ${
          isVipActive
            ? 'text-[#FFD700] border-[#FFD700]/30 bg-[#FFD700]/10'
            : isUnlocked
            ? 'text-[#00FF88] border-[#00FF88]/30 bg-[#00FF88]/10'
            : 'text-[#00D1FF] border-[#00D1FF]/30 bg-[#00D1FF]/10'
        }`}
      >
        {badgeText}
      </div>
    </button>
  );
};

export default QualityButton;
