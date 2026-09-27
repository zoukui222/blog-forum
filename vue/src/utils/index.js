export function detectDevice() {
    const ua = navigator.userAgent;
    const isMobile = /Android|webOS|iPhone|iPad|iPod|Harmony|BlackBerry|IEMobile|Opera Mini/i.test(ua);
    return isMobile ? 'Mobile' : 'PC';
}

