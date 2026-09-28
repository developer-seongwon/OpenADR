import React from 'react';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';

// material-ui-color-picker(3.x) 를 대신하는 컴포넌트.
//
// 그 패키지는 MUI 3 과 react-color 에 묶여 있어서 MUI 9, React 19 로 가면서 뺐다.
// 쓰는 곳은 마켓 컨텍스트 생성 화면 한 군데다. 예전처럼 hex 문자열을 글자로 보여주고
// 글자 색도 그 색으로 칠한다. 색을 고르는 창은 브라우저 기본 색상 선택기(input type=color)를 쓴다.
// onChange 는 예전과 같이 '#rrggbb' 문자열 하나로 불린다
export default function ColorPicker( { label, value, onChange, className, style } ) {
  // input type=color 는 #rrggbb 만 받는다. 비어 있거나 모양이 다르면 검정으로 보여준다
  const pickerValue = /^#[0-9a-fA-F]{6}$/.test( value || '' ) ? value : '#000000';
  return (
    <TextField variant="standard"
               label={ label }
               value={ value || '' }
               placeholder="#000000"
               onChange={ ( e ) => onChange( e.target.value ) }
               className={ className }
               style={ style }
               slotProps={ {
                 inputLabel: { shrink: true },
                 input: {
                   style: { color: value },
                   endAdornment: (
                     <InputAdornment position="end">
                       <input type="color"
                              aria-label={ label }
                              value={ pickerValue }
                              onChange={ ( e ) => onChange( e.target.value ) }
                              style={ { width: 32, height: 24, padding: 0, border: 'none', background: 'none', cursor: 'pointer' } } />
                     </InputAdornment>
                   ),
                 },
               } } />
  );
}
