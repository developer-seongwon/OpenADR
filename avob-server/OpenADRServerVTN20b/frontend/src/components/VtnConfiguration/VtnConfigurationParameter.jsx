import React, { useState } from 'react';
import { t } from '../../i18n';

import TextField from '@mui/material/TextField';
import FormControl from '@mui/material/FormControl';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';

import Clear from '@mui/icons-material/Clear';
import Done from '@mui/icons-material/Done';
import ContentCopy from '@mui/icons-material/ContentCopy';

import Divider from '@mui/material/Divider';



// 복사 칸의 폭. 2.0b 엔드포인트가 한 줄에 다 보이게 넓게 둔다. 칸 자체는 fullWidth 라 바깥 여백은 FormControl(formControl)이 준다
const COPY_FIELD_STYLE = { width: 640, maxWidth: '100%' };

function VtnConfigurationTextField( props ) {
  return (
    <TextField label={ props.field }
               defaultValue={ props.value }
               className={ props.className }
               slotProps={{
                 input: { readOnly: true, }
               }} />
  );
}

function VtnConfigurationFeatureField( props ) {
  var str = (props.value) ? t( 'vtnConfig.supported' ) : t( 'vtnConfig.notSupported' )
  var ico = (props.value) ? <Done color="action" /> : <Clear color="action" />
  return (
    <TextField label={ props.field }
               defaultValue={ str }
               className={ props.className }
               slotProps={{
                 input: { readOnly: true, endAdornment: ( <InputAdornment> { ico } </InputAdornment> ), }
               }} />
  );
}

// VEN 쪽 설정에 옮겨 적을 값. 오른쪽 버튼으로 복사한다.
// navigator.clipboard 는 https 화면에서만 있다(VTN 화면은 https). 없으면 칸을 눌러 글자를 골라 복사한다
function VtnConfigurationCopyField( { label, value, helperText, className } ) {
  const [ copied, setCopied ] = useState( false );
  const copy = () => {
    if ( !navigator.clipboard ) {
      return;
    }
    navigator.clipboard.writeText( value ).then( () => setCopied( true ), () => setCopied( false ) );
  };
  return (
    <TextField label={ label }
               value={ value }
               helperText={ helperText }
               className={ className }
               fullWidth={ true }
               onFocus={ ( e ) => e.target.select() }
               slotProps={{
                 inputLabel: { shrink: true },
                 input: {
                   readOnly: true,
                   endAdornment: (
                     <InputAdornment position="end">
                       <Tooltip title={ copied ? t( 'vtnConfig.copied' ) : t( 'vtnConfig.copy' ) }
                                onClose={ () => setCopied( false ) }>
                         <IconButton size="small" aria-label={ t( 'vtnConfig.copy' ) } onClick={ copy }>
                           <ContentCopy fontSize="small" />
                         </IconButton>
                       </Tooltip>
                     </InputAdornment>
                   ),
                 }
               }} />
  );
}

// VEN 이 붙는 VTN 주소의 앞부분(https://호스트:포트/testvtn).
// 서버는 이 서버의 바깥 주소를 모른다(VtnConfigurationDto.host 는 늘 비어 있어서 예전에는 엔드포인트 칸이 아예 안 보였다).
// 그래서 지금 이 화면에 들어온 주소로 만든다. 서버 IP 가 바뀌어도 새 주소로 들어오면 따라간다.
// 포트도 서버 설정(oadr.server.port)이 아니라 브라우저 주소를 쓴다. 컨테이너 포트를 호스트에 다른 포트로 열 수 있어서다.
// context path 는 서버 설정을 쓰고, 아직 못 받았으면 이 화면이 실린 경로(vite base)를 쓴다
function vtnBaseUrl( contextPath ) {
  var path = ( contextPath != null ) ? contextPath : import.meta.env.BASE_URL.replace( /\/$/, '' );
  return window.location.origin + path;
}

const VtnConfigurationParameter = (props) => {
  const {classes, vtnConfiguration, marketContext} = props;

  var getTextField = function ( label, field ) {
    var view = null;
    var value = props.vtnConfiguration[ field ];
    if ( props.vtnConfiguration[ field ] != null ) {
      view = (
        <VtnConfigurationTextField className={ classes.textField }
                                   field={ label }
                                   value={ value } />
      )
    }
    return view;
  }

  var getFeatureField = function ( label, field ) {
    var view = null;
    var value = props.vtnConfiguration[ field ];
    if ( props.vtnConfiguration[ field ] != null ) {
      view = (
        <VtnConfigurationFeatureField className={ classes.textField }
                                      field={ label }
                                      value={ value } />
      )
    }
    return view;
  }

  var baseUrl = vtnBaseUrl( vtnConfiguration.contextPath );
  var endpoint20b = baseUrl + '/OpenADR2/Simple/2.0b';
  var endpoint20a = baseUrl + '/OpenADR2/Simple';

  // VEN 쪽 마켓 컨텍스트 설정에 넣을 이름. Market-Context 탭에서 만든 것들
  var marketContextView = [];
  for (var i in marketContext) {
    var context = marketContext[ i ];
    marketContextView.push(
      <div key={ 'copy_marketcontext_' + context.id }>
        <FormControl className={ classes.formControl } style={ COPY_FIELD_STYLE }>
          <VtnConfigurationCopyField label={ t( 'card.marketContext' ) }
                                     value={ context.name || '' }
                                     helperText={ context.description } />
        </FormControl>
      </div>
    );
  }

  return (
  <div className={ classes.root }>
    <Typography variant="subtitle1">
      { t( 'vtnConfig.venInfo.title' ) }
    </Typography>
    <Typography variant="body2" sx={ { color: 'text.secondary', mb: 1 } }>
      { t( 'vtnConfig.venInfo.description' ) }
    </Typography>
    <div>
      <FormControl className={ classes.formControl } style={ COPY_FIELD_STYLE }>
        <VtnConfigurationCopyField label={ t( 'vtnConfig.vtnId' ) }
                                   value={ vtnConfiguration.vtnId || '' }
                                   helperText={ t( 'vtnConfig.vtnIdHelp' ) } />
      </FormControl>
    </div>
    <div>
      <FormControl className={ classes.formControl } style={ COPY_FIELD_STYLE }>
        <VtnConfigurationCopyField label={ t( 'vtnConfig.endpoint20b' ) }
                                   value={ endpoint20b }
                                   helperText={ t( 'vtnConfig.endpointHelp' ) } />
      </FormControl>
    </div>
    { marketContextView.length > 0
        ? marketContextView
        : <Typography variant="body2" sx={ { color: 'text.secondary', m: 1 } }>{ t( 'vtnConfig.noMarketContext' ) }</Typography> }
    <Divider style={ { marginBottom: '20px', marginTop: '20px' } } />
    {/* 서버는 초로 준다. VTN 이 등록 응답(oadrRequestedOadrPollFreq)에 싣는 모양과 같게 ISO-8601 기간(PT10S)으로 보여 준다 */}
    <FormControl className={ classes.formControl }>
      { vtnConfiguration.pullFrequencySeconds != null
          ? <VtnConfigurationTextField className={ classes.textField }
                                       field={ t( 'vtnConfig.pullFrequency' ) }
                                       value={ 'PT' + vtnConfiguration.pullFrequencySeconds + 'S' } />
          : null }
    </FormControl>
    <FormControl className={ classes.formControl }>
      { getTextField( t( 'vtnConfig.fingerprint' ), 'fingerprint' ) }
    </FormControl>
    <FormControl className={ classes.formControl } key="textfield_endpoint20a">
      <VtnConfigurationTextField className={ classes.textField }
                                 field={ t( 'vtnConfig.endpoint20a' ) }
                                 value={ endpoint20a } />
    </FormControl>
    <Divider style={ { marginBottom: '20px', marginTop: '20px' } } />
    <FormControl className={ classes.formControl }>
      { getFeatureField( t( 'vtnConfig.httpsPush' ), 'supportPush' ) }
    </FormControl>
    <FormControl className={ classes.formControl }>
      { getFeatureField( t( 'vtnConfig.unsecuredHttpPush' ), 'supportUnsecuredHttpPush' ) }
    </FormControl>
  </div>
  );
};

export default VtnConfigurationParameter;
