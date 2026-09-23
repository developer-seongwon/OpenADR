import React from 'react';

// 카드(VEN, 이벤트, 마켓 컨텍스트, 그룹)를 가로로 채우다가 넘치면 줄을 바꿔 나열한다.
//
// 예전에는 MUI 3 의 GridList 로 감쌌다. GridList 는 flex-wrap 목록이라 카드가 제 폭대로 붙어 섰다.
// MUI 5 에서 GridList 가 ImageList 로 바뀌면서 CSS grid(기본 2칸 균등 분할)가 되어,
// 카드가 화면 폭에 맞춰 양쪽으로 벌어졌다. 예전 GridList 의 스타일을 그대로 옮겨 모양을 되돌린다
const style = {
  display: 'flex',
  flexWrap: 'wrap',
  justifyContent: 'left',
  overflowY: 'auto',
  listStyle: 'none',
  padding: 0,
};

export default function CardList( { children } ) {
  return <div style={ style }>{ children }</div>;
}
