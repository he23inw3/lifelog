import { Smile, Frown, Meh, Compass } from 'lucide-react';

/**
 * 感情ラベルに対応するアイコンコンポーネントを取得します。
 *
 * @param sentiment - 感情文字列 ('happy', 'tired', 'stressed', 'neutral' など)
 * @param size - アイコンのサイズ（デフォルト値は 20）
 * @returns 該当する感情の lucide-react アイコン要素
 */
export const getSentimentIcon = (sentiment: string, size = 20) => {
  const s = sentiment?.toLowerCase();
  if (s === 'happy') return <Smile size={size} color="#2e7d32" />;
  if (s === 'tired' || s === 'stressed') return <Frown size={size} color="#d32f2f" />;
  if (s === 'neutral') return <Meh size={size} color="#0288d1" />;
  return <Compass size={size} color="#757575" />;
};

/**
 * 感情ラベルの表示用テキスト（絵文字付き）を取得します。
 *
 * @param sentiment - 感情文字列 ('happy', 'tired', 'stressed', 'neutral' など)
 * @returns 絵文字を含んだわかりやすい表示用文字列
 */
export const getSentimentText = (sentiment: string) => {
  const s = sentiment?.toLowerCase();
  if (s === 'happy') return 'Happy 😊';
  if (s === 'tired') return 'Tired 😫';
  if (s === 'stressed') return 'Stressed 😡';
  if (s === 'neutral') return 'Neutral 😐';
  return sentiment || 'Neutral';
};
