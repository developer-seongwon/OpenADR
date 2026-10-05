import React from 'react';
import { t } from '../../i18n';
import TextField from '@mui/material/TextField';
import Autocomplete from '@mui/material/Autocomplete';

// 서버에서 찾아 온 목록을 고르는 입력칸.
//
// 예전에는 react-autosuggest 가 그린 목록 안에 MenuItem 을 넣었다. MUI 9 의 MenuItem 은 Menu, MenuList 안에서만
// 쓸 수 있어서(MenuListContext) 목록이 뜨는 순간 "MenuListContext is missing" 을 던지고 화면 전체가 죽었다
// (이벤트 만들기 대상 단계, 이벤트 상세 대상 탭, 이벤트 목록의 VEN 필터).
// MUI Autocomplete 로 바꿨다. 찾기는 서버가 하므로 filterOptions 는 받은 목록을 그대로 둔다.
// 부르는 쪽 약속은 예전과 같다. onSuggestionsFetchRequested({value}), onSuggestionsSelect(고른 것 또는 지우면 null)
function SearchAutocomplete( props ) {
  const { suggestions, onSuggestionsFetchRequested, onSuggestionsClearRequested, onSuggestionsSelect,
    optionLabel, optionKey, renderOptionContent, label } = props;
  const [ inputValue, setInputValue ] = React.useState( '' );

  const fetch = ( value ) => {
    if ( onSuggestionsFetchRequested ) {
      onSuggestionsFetchRequested( { value: value } );
    }
  };

  return (
    <Autocomplete
      fullWidth
      options={ Array.isArray( suggestions ) ? suggestions : [] }
      filterOptions={ ( options ) => options }
      getOptionLabel={ ( option ) => ( option ? optionLabel( option ) : '' ) }
      isOptionEqualToValue={ ( option, value ) => optionKey( option ) === optionKey( value ) }
      inputValue={ inputValue }
      onInputChange={ ( event, value, reason ) => {
        setInputValue( value );
        if ( reason === 'input' ) {
          fetch( value );
        }
        if ( reason === 'clear' && onSuggestionsClearRequested ) {
          onSuggestionsClearRequested();
        }
      } }
      onOpen={ () => fetch( inputValue ) }
      onChange={ ( event, value ) => {
        if ( onSuggestionsSelect ) {
          onSuggestionsSelect( value );
        }
      } }
      renderOption={ ( optionProps, option ) => {
        // MUI 가 key 를 props 에 같이 넣어 준다. 펼쳐 넘기면 React 가 경고하므로 따로 준다
        const { key, ...rest } = optionProps;
        return (
          <li key={ key } { ...rest }>
            { renderOptionContent( option ) }
          </li>
        );
      } }
      noOptionsText={ t( 'common.noOptions' ) }
      renderInput={ ( params ) => <TextField { ...params } label={ label } /> } />
  );
}

var venLabel = ( ven ) => ( ven.commonName ? ven.commonName + ' (' + ven.username + ')' : ven.username );

export function VenAutocomplete( props ) {
  return (
    <SearchAutocomplete { ...props }
      label={ t( 'event.venSearch' ) }
      optionLabel={ venLabel }
      optionKey={ ( ven ) => ven.username }
      renderOptionContent={ ( ven ) => (
        <div>
          <strong>{ ven.commonName }</strong> (<i>{ ven.username }</i>)
        </div>
      ) } />
  );
}
