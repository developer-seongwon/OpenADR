import React, { useState } from 'react';
import TextField from '@mui/material/TextField';
import Chip from '@mui/material/Chip';

// material-ui-chip-input(1.x) 를 대신하는 컴포넌트.
//
// 그 패키지는 MUI 3 까지만 지원하고 2019년 이후 멈춰서 MUI 9, React 19 에서 쓸 수 없다.
// 쓰는 곳 세 군데(FilterPanel, VenDetailGroup, VenDetailEnrollment)가 기대하는 동작만 옮겼다.
//
//   value     칩으로 보여줄 값 배열. 문자열이나 React 요소(FilterPanel 은 아이콘이 붙은 요소를 넘긴다)
//   onAdd     입력창에서 Enter 를 누르면 입력한 문자열로 불린다
//   onDelete  칩의 x 를 누르거나, 입력이 빈 채로 Backspace 를 누르면 (칩 값, 위치) 로 불린다
//   InputLabelProps  예전 이름 그대로 받아서 MUI 9 의 slotProps.inputLabel 로 넘긴다
//
// 칩 목록 자체는 들고 있지 않는다. 예전처럼 부모가 value 를 바꿔 줘야 화면에 반영된다
export default function ChipInput( {
  label, placeholder, value = [], onAdd, onDelete, fullWidth, className, style, InputLabelProps,
} ) {
  const [ text, setText ] = useState( '' );

  const handleKeyDown = ( e ) => {
    if ( e.key === 'Enter' ) {
      // 폼 안에 있으면 Enter 가 submit 으로 번지지 않게 막는다
      e.preventDefault();
      const v = text.trim();
      if ( v !== '' && onAdd ) {
        onAdd( v );
      }
      setText( '' );
    } else if ( e.key === 'Backspace' && text === '' && value.length > 0 && onDelete ) {
      onDelete( value[ value.length - 1 ], value.length - 1 );
    }
  };

  const chips = value.map( ( chip, index ) => (
    <Chip key={ index }
          label={ chip }
          onDelete={ onDelete ? () => onDelete( chip, index ) : undefined }
          style={ { margin: '4px 8px 4px 0' } } />
  ) );

  return (
    <TextField variant="standard"
               label={ label }
               placeholder={ placeholder }
               value={ text }
               onChange={ ( e ) => setText( e.target.value ) }
               onKeyDown={ handleKeyDown }
               // 예전 패키지 기본값(blurBehavior="clear")처럼 포커스를 잃으면 입력 중이던 글자를 버린다
               onBlur={ () => setText( '' ) }
               fullWidth={ fullWidth }
               className={ className }
               style={ style }
               slotProps={ {
                 // 칩이 있으면 라벨이 칩과 겹치지 않게 위로 올린다
                 inputLabel: { ...( chips.length > 0 ? { shrink: true } : {} ), ...InputLabelProps },
                 // startAdornment 는 칩이 있을 때만 준다. 빈 배열이라도 넘기면 MUI 가 앞장식이 있다고 보고
                 // 라벨을 항상 위로 올려서, 칩이 없을 때도 라벨과 placeholder 가 겹쳐 보였다
                 input: { startAdornment: chips.length > 0 ? chips : undefined, style: { flexWrap: 'wrap' } },
                 // 입력칸은 기본이 width 100% 라 칩 옆에 못 붙고 다음 줄로 떨어진다. 남는 폭만 쓰게 한다
                 htmlInput: { style: { flex: '1 1 60px', width: 'auto', minWidth: 60 } },
               } } />
  );
}
