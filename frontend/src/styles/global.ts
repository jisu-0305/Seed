import { css, SerializedStyles, Theme } from '@emotion/react';

export const globalStyles = (theme: Theme): SerializedStyles => css`
  html,
  body,
  div,
  span,
  applet,
  object,
  iframe,
  h1,
  h2,
  h3,
  h4,
  h5,
  h6,
  p,
  blockquote,
  pre,
  a,
  abbr,
  acronym,
  address,
  big,
  cite,
  code,
  del,
  dfn,
  em,
  img,
  ins,
  kbd,
  q,
  s,
  samp,
  small,
  strike,
  strong,
  sub,
  sup,
  tt,
  var,
  b,
  u,
  i,
  center,
  dl,
  dt,
  dd,
  menu,
  ol,
  ul,
  li,
  fieldset,
  form,
  label,
  legend,
  table,
  caption,
  tbody,
  tfoot,
  thead,
  tr,
  th,
  td,
  article,
  aside,
  canvas,
  details,
  embed,
  figure,
  figcaption,
  footer,
  header,
  hgroup,
  main,
  menu,
  nav,
  section,
  summary,
  time,
  mark,
  audio,
  video {
    margin: 0;
    padding: 0;
    border: 0;
    font-size: 60%;
    vertical-align: baseline;
    color: ${theme.colors.Text};
  }

  article,
  aside,
  details,
  figcaption,
  figure,
  footer,
  header,
  hgroup,
  main,
  menu,
  nav,
  section {
    display: block;
  }

  *[hidden] {
    display: none;
  }

  body {
    line-height: 1;
  }

  menu,
  ol,
  ul {
    list-style: none;
  }

  blockquote,
  q {
    quotes: none;
  }

  blockquote:before,
  blockquote:after,
  q:before,
  q:after {
    content: '';
    content: none;
  }

  table {
    border-collapse: collapse;
    border-spacing: 0;
  }

  button {
    padding: 0;
    background: transparent;
    border: none;
    cursor: pointer;
  }

  input {
    border: none;
    background: none;

    &:focus {
      outline: none;
    }
  }

  hr {
    border: none;
    border-top: 0.06rem solid ${theme.colors.BorderDefault};
    width: 100%;
  }

  #root,
  body,
  html {
    width: 100vw;
    height: 100vh;
    margin: 0 auto;
    overflow-y: auto;

    background-color: ${theme.colors.Background};

    -ms-overflow-style: none; /* IE */
    scrollbar-width: none; /* Firefox */
  }

  #root::-webkit-scrollbar {
    display: none; /* Chrome, Safari, Opera, Edge */
  }
`;
