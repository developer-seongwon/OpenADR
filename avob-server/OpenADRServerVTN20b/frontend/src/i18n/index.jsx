import React, { useSyncExternalStore } from 'react';

import ko from './ko';
import en from './en';

// 화면 문구 번역. 라이브러리(react-i18next 등) 없이 사전 두 개와 t() 하나로 한다.
//
// 화면 대부분이 클래스 컴포넌트라 훅(useTranslation)을 쓸 수 없다. 그래서 t() 는 어디서나 부를 수 있는
// 보통 함수로 두고, 언어가 바뀌면 LanguageProvider 가 아래 화면을 통째로 다시 마운트한다.
// 리덕스 상태와 로그인 정보(config)는 화면 밖에 있어서 그대로 남는다. 다시 마운트되며 목록 조회가 한 번 더 나가고,
// 작성 중이던 입력(이벤트 만들기 단계 등)은 사라진다.
//
// 번역하지 않는 것: 날짜 형식과 캘린더(dayjs, react-big-calendar 는 영어 그대로),
// OpenADR 용어(VEN, VTN, MarketContext, eiEvent 같은 것)와 서버가 준 값(상태, 역할 이름 등)

export const LANGUAGES = [
  { code: 'ko', label: '한국어' },
  { code: 'en', label: 'English' },
];

const MESSAGES = { ko, en };

// 브라우저에 고른 언어를 남기는 키. 사생활 모드나 저장소가 막힌 브라우저에서는 읽기와 쓰기가 예외를 던질 수 있다
const STORAGE_KEY = 'vtn.language';

function readStoredLanguage() {
  try {
    var stored = window.localStorage.getItem( STORAGE_KEY );
    return MESSAGES[ stored ] ? stored : null;
  } catch ( e ) {
    return null;
  }
}

function writeStoredLanguage( lang ) {
  try {
    window.localStorage.setItem( STORAGE_KEY, lang );
  } catch ( e ) {
    // 저장이 안 되면 이번 탭에서만 바뀐다
  }
}

// 저장된 값이 없으면 브라우저 언어를 따른다. 한국어가 아니면 영어
function detectBrowserLanguage() {
  var list = ( navigator.languages && navigator.languages.length ) ? navigator.languages : [ navigator.language ];
  for ( var i = 0; i < list.length; i++ ) {
    var code = ( list[ i ] || '' ).toLowerCase();
    if ( code.startsWith( 'ko' ) ) {
      return 'ko';
    }
    if ( code.startsWith( 'en' ) ) {
      return 'en';
    }
  }
  return 'en';
}

let current = readStoredLanguage() || detectBrowserLanguage();
document.documentElement.lang = current;

const listeners = new Set();

function subscribe( listener ) {
  listeners.add( listener );
  return () => listeners.delete( listener );
}

export function getLanguage() {
  return current;
}

export function setLanguage( lang ) {
  if ( !MESSAGES[ lang ] || lang === current ) {
    return;
  }
  current = lang;
  writeStoredLanguage( lang );
  document.documentElement.lang = lang;
  listeners.forEach( ( listener ) => listener() );
}

// t( 'ven.list.title' ), t( 'common.selected', { count: 3 } ).
// 지금 언어에 없으면 영어, 영어에도 없으면 키를 그대로 보여 준다(빠진 번역이 화면에서 바로 보이게)
export function t( key, params ) {
  var text = MESSAGES[ current ][ key ];
  if ( text === undefined ) {
    text = MESSAGES.en[ key ];
  }
  if ( text === undefined ) {
    return key;
  }
  if ( params ) {
    text = text.replace( /\{(\w+)\}/g, ( whole, name ) => ( params[ name ] !== undefined ? params[ name ] : whole ) );
  }
  return text;
}

// 함수 컴포넌트에서 지금 언어를 읽고, 바뀌면 다시 그린다
export function useLanguage() {
  return useSyncExternalStore( subscribe, getLanguage );
}

// 언어가 바뀌면 key 가 바뀌어서 children 전체가 새로 마운트된다. 위 설명 참고
export function LanguageProvider( { children } ) {
  const lang = useLanguage();
  return <React.Fragment key={ lang }>{ children }</React.Fragment>;
}
