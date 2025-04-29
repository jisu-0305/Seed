import { css } from '@emotion/react';
import { Inter, Noto_Sans_KR } from 'next/font/google';

const notoSansKr = Noto_Sans_KR({
  weight: ['100', '300', '400', '500', '700', '900'],
  subsets: ['latin'],
  display: 'swap',
  fallback: ['sans-serif'],
});

const inter = Inter({
  weight: ['100', '300', '400', '500', '700', '900'],
  subsets: ['latin'],
  display: 'swap',
  fallback: ['sans-serif'],
});

export const fonts = {
  Head0: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 3rem;
    font-style: normal;
    font-weight: 700;
    line-height: 130%; /* 5.2rem */
  `,
  Head1: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 2.4rem;
    font-style: normal;
    font-weight: 700;
    line-height: 130%; /* 4.16rem */
  `,
  Head2: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 2rem;
    font-style: normal;
    font-weight: 700;
    line-height: 130%; /* 3.9rem */
  `,
  Head3: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.8rem;
    font-style: normal;
    font-weight: 700;
    line-height: 130%; /* 3.38rem */
  `,
  Head4: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.6rem;
    font-style: normal;
    font-weight: 700;
    line-height: 130%; /* 3.12rem */
  `,
  Head5: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.4rem;
    font-style: normal;
    font-weight: 700;
    line-height: 130%; /* 2.6rem */
  `,
  Head6: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.2rem;
    font-style: normal;
    font-weight: 700;
    line-height: 130%; /* 2.34rem */
  `,
  Title1: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 4rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 2.08rem */
  `,
  Title2: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 3rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 2.08rem */
  `,
  Title3: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 2.4rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 2.08rem */
  `,
  Title4: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.8rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 2.08rem */
  `,
  Title5: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.6rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 2.08rem */
  `,
  Title6: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.4rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 2.08rem */
  `,
  Title7: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.2rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 2.08rem */
  `,
  Title8: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.1rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 2.08rem */
  `,
  Title9: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 2.08rem */
  `,
  Body1: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.6rem;
    font-style: normal;
    font-weight: 400;
    line-height: 130%; /* 2.08rem */
  `,
  Body2: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.5rem;
    font-style: normal;
    font-weight: 400;
    line-height: 130%; /* 1.69rem */
  `,
  Body3: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.4rem;
    font-style: normal;
    font-weight: 400;
    line-height: 130%; /* 1.82rem */
  `,
  Body4: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.3rem;
    font-style: normal;
    font-weight: 400;
    line-height: 130%; /* 1.56rem */
  `,
  Body5: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1.2rem;
    font-style: normal;
    font-weight: 400;
    line-height: 130%; /* 1.56rem */
  `,
  Body6: css`
    font-family: ${notoSansKr.style.fontFamily};
    font-size: 1rem;
    font-style: normal;
    font-weight: 400;
    line-height: 130%; /* 1.17rem */
  `,
  EnHead0: css`
    font-family: ${inter.style.fontFamily};
    font-size: 2rem;
    font-style: normal;
    font-weight: 700;
    line-height: 130%; /* 1.17rem */
  `,
  EnTitle0: css`
    font-family: ${inter.style.fontFamily};
    font-size: 3.6rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 1.17rem */
  `,
  EnTitle1: css`
    font-family: ${inter.style.fontFamily};
    font-size: 3rem;
    font-style: normal;
    font-weight: 600;
    line-height: 130%; /* 1.17rem */
  `,
  EnTitle2: css`
    font-family: ${inter.style.fontFamily};
    font-size: 2rem;
    font-style: normal;
    font-weight: 500;
    line-height: 130%; /* 1.17rem */
  `,
  EnTitle3: css`
    font-family: ${inter.style.fontFamily};
    font-size: 1.4rem;
    font-style: normal;
    font-weight: 500;
    line-height: 130%; /* 1.17rem */
  `,
  EnTitle4: css`
    font-family: ${inter.style.fontFamily};
    font-size: 1.2rem;
    font-style: normal;
    font-weight: 500;
    line-height: 130%; /* 1.17rem */
  `,
  EnBody1: css`
    font-family: ${inter.style.fontFamily};
    font-size: 1.4rem;
    font-style: normal;
    font-weight: 400;
    line-height: 130%; /* 1.17rem */
  `,
  EnBody2: css`
    font-family: ${inter.style.fontFamily};
    font-size: 1.2rem;
    font-style: normal;
    font-weight: 400;
    line-height: 130%; /* 1.17rem */
  `,
  EnBody3: css`
    font-family: ${inter.style.fontFamily};
    font-size: 1rem;
    font-style: normal;
    font-weight: 400;
    line-height: 130%; /* 1.17rem */
  `,
};

export const colors = {
  Main_Carrot: '#FFA100',
  Carrot0: '#CA9843',
  Carrot2: '#FFC766',
  Black: '#000000',
  Black1: '#1C1C1C',
  Gray0: '#323232',
  Gray1: '#484848',
  Gray2: '#666666',
  Gray3: '#969696',
  Gray4: '#F7F9FB',
  LightGray1: '#E9E9E9',
  LightGray2: '#EDEDED',
  LightGray3: '#FCFCFC',
  White: '#FFFFFF',
  DarkGray0: '#252525',
  DarkGray1: '#3D3D3D',
  DarkGray2: '#D3D3D3',
  Blue0: '#4C6589',
  Blue1: '#4B7BBF',
  Blue2: '#A6C9FB',
  Blue3: '#E3F5FF',
  Blue4: '#E5ECF6',
  Red0: '#9D3D3D',
  Red1: '#A74A4A',
  Red2: '#E05B5B',
  Red3: '#F2B9B9',
  Red4: '#FFE9E9',
  Purple1: '#4648B5',
  Purple2: '#7577D8',
  Purple3: '#95A4FC',
  Purple4: '#C6C7F8',
  Purple5: '#F2F2FF',
  Green0: '#5B7A6F',
  Green1: '#4AA785',
  Green2: '#98CEBA',
};

export const theme = {
  colors,
  fonts,
};
