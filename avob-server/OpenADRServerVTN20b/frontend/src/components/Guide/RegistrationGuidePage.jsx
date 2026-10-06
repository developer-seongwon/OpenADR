import React from 'react';
import { Link } from 'react-router';
import { t } from '../../i18n';

import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Alert from '@mui/material/Alert';

// VEN 을 이 VTN 에 붙여 이벤트를 받기까지의 순서를 적은 화면. API 를 부르지 않는 정적 문서다.
// 문구는 i18n 의 guide.* 키에 있고, 단계 순서와 어느 문구를 어디에 둘지는 아래 STEPS 가 정한다.
// side 는 그 단계를 어디서 하는지다(vtn 은 이 관리 화면, ven 은 VEN 쪽 시스템)

// VEN 만들기 끝에 받는 tar 안의 파일. 이름은 VTN 이 정한다(GenerateX509CertificateService). 이름은 번역하지 않는다
const CREDENTIAL_FILES = [
  { name: '<Common Name>.crt', text: 'guide.file.crt' },
  { name: '<Common Name>.key', text: 'guide.file.key' },
  { name: 'ROOT_CA.crt', text: 'guide.file.ca' },
  { name: '<Common Name>.fingerprint', text: 'guide.file.fingerprint' },
];

const STEPS = [
  {
    side: 'vtn',
    title: 'guide.marketContext.title',
    items: [ 'guide.marketContext.what', 'guide.marketContext.name' ],
    link: { to: '/vtn_configuration/marketcontext', text: 'guide.marketContext.link' },
  },
  {
    side: 'vtn',
    title: 'guide.createVen.title',
    items: [ 'guide.createVen.profile', 'guide.createVen.cert', 'guide.createVen.download' ],
    files: true,
    note: 'guide.createVen.once',
    link: { to: '/ven/create', text: 'guide.createVen.link' },
  },
  {
    side: 'vtn',
    title: 'guide.enroll.title',
    items: [ 'guide.enroll.how', 'guide.enroll.group' ],
    note: 'guide.enroll.before',
    link: { to: '/ven', text: 'guide.enroll.link' },
  },
  {
    side: 'ven',
    title: 'guide.venConfig.title',
    items: [ 'guide.venConfig.cert', 'guide.venConfig.url', 'guide.venConfig.vtnId', 'guide.venConfig.venId', 'guide.venConfig.transport' ],
    link: { to: '/vtn_configuration/parameter', text: 'guide.venConfig.link' },
  },
  {
    side: 'ven',
    title: 'guide.register.title',
    items: [ 'guide.register.create', 'guide.register.again', 'guide.register.check', 'guide.register.report' ],
    link: { to: '/ven', text: 'guide.register.link' },
  },
  {
    side: 'vtn',
    title: 'guide.event.title',
    items: [ 'guide.event.create', 'guide.event.target', 'guide.event.publish', 'guide.event.timing', 'guide.event.check' ],
    link: { to: '/event/create', text: 'guide.event.link' },
  },
];

const TROUBLES = [ 'guide.trouble.tls', 'guide.trouble.auth', 'guide.trouble.register', 'guide.trouble.noEvent' ];

function CredentialFiles() {
  return (
    <Box component="ul" sx={ { pl: 3, mt: 0, mb: 1 } }>
      { CREDENTIAL_FILES.map( ( file ) => (
        <li key={ file.name }>
          <Typography variant="body2" component="span" sx={ { fontFamily: 'monospace', mr: 1 } }>
            { file.name }
          </Typography>
          <Typography variant="body2" component="span">
            { t( file.text ) }
          </Typography>
        </li>
      ) ) }
    </Box>
  );
}

function GuideStep( { number, step } ) {
  return (
    <Paper variant="outlined" sx={ { p: 3, mb: 2 } }>
      <Box sx={ { display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 } }>
        <Typography variant="h6" component="h2">
          { number }. { t( step.title ) }
        </Typography>
        <Chip size="small"
              variant="outlined"
              color={ step.side === 'ven' ? 'secondary' : 'primary' }
              label={ t( 'guide.side.' + step.side ) } />
      </Box>
      { step.items.map( ( key ) => (
        <Typography key={ key } variant="body2" sx={ { mb: 1 } }>
          { t( key ) }
        </Typography>
      ) ) }
      { step.files ? <CredentialFiles /> : null }
      { step.note ? <Alert severity="warning" sx={ { mb: 1 } }>{ t( step.note ) }</Alert> : null }
      { step.link ? (
        <Button component={ Link } to={ step.link.to } variant="outlined" size="small" sx={ { mt: 1 } }>
          { t( step.link.text ) }
        </Button>
      ) : null }
    </Paper>
  );
}

const RegistrationGuidePage = () => {
  return (
    <Box sx={ { maxWidth: 960 } }>
      <Typography variant="h5" component="h1" gutterBottom>
        { t( 'guide.title' ) }
      </Typography>
      <Typography variant="body1" sx={ { mb: 3 } }>
        { t( 'guide.intro' ) }
      </Typography>
      { STEPS.map( ( step, index ) => (
        <GuideStep key={ step.title } number={ index + 1 } step={ step } />
      ) ) }
      <Paper variant="outlined" sx={ { p: 3, mb: 2 } }>
        <Typography variant="h6" component="h2" sx={ { mb: 1.5 } }>
          { t( 'guide.trouble.title' ) }
        </Typography>
        <Box component="ul" sx={ { pl: 3, my: 0 } }>
          { TROUBLES.map( ( key ) => (
            <li key={ key }>
              <Typography variant="body2" sx={ { mb: 1 } }>
                { t( key ) }
              </Typography>
            </li>
          ) ) }
        </Box>
      </Paper>
    </Box>
  );
};

export default RegistrationGuidePage;
