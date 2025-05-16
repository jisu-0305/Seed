/** @type {import('next').NextConfig} */

import withPWA from 'next-pwa';

const nextPWA = withPWA({
  dest: 'public',
  register: true,
  // disable: process.env.NODE_ENV === 'development',
  swSrc: '/firebase-messaging-sw.mjs',
  scope: '/',
  sw: 'sw.js',
});

const nextConfig = {
  reactStrictMode: true,
  // ...
};

export default nextPWA(nextConfig);
