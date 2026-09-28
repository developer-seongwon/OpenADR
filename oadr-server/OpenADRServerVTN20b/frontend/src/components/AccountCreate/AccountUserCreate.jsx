import React from 'react';
import { t } from '../../i18n';




import Stepper from '@mui/material/Stepper';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';


import Paper from '@mui/material/Paper';

import AccountUserCreateIdentificationStep from './AccountUserCreateIdentificationStep'
import UserCreateAuthenticationFormPanel from '../common/UserCreateAuthenticationFormPanel'
import AccountCreateRoleStep from './AccountCreateRoleStep'
import UserCreateConfirmationFormPanel from '../common/UserCreateConfirmationFormPanel'

const defaultAuthenticationType = 'x509'
const authenticationTypes = {
  x509: {
    // 모듈을 읽을 때가 아니라 화면에 그릴 때 번역하도록 getter 로 둔다
    get label() { return t( 'auth.type.x509' ); }
  },
  login: {
    get label() { return t( 'auth.type.login' ); }
  }
}

function getSteps() {
  return [ t( 'accountCreate.step.identification' ), t( 'accountCreate.step.authentication' ),
    t( 'accountCreate.step.roles' ), t( 'accountCreate.step.confirmation' ) ];
}


export class AccountUserCreate extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {
      activeStep: 0,
      identification: {
        commonName: "",
        needCertificateGeneration: 'rsa'
      },
      authentication: {
          username: '',
          authenticationTypes:authenticationTypes,
          authenticationType: defaultAuthenticationType,
          authenticationPassword: '',
          authenticationPasswordConfirm: '',
          
      },
      roles: []
    };
  }

  handleCreateUser = () => {
    const {identification, authentication, roles} = this.state;
    var dto = {
      'commonName': identification.venCommonName,
      'needCertificateGeneration': identification.needCertificateGeneration,
      'oadrProfil': identification.venOadrProfile,

      'authenticationType': authentication.authenticationType,
      'username': authentication.username,
      'roles': roles
    }

    if ( authentication.authenticationType === 'login' ) {
      dto.password = authentication.authenticationPassword
    }

    this.props.createUser( dto );
  }


  handleFinish = () => {
    this.handleCreateUser();
  }

  handleNext = () => {
    const {activeStep} = this.state;
    if ( activeStep === getSteps().length - 1 ) {
      this.handleFinish();
    } else {
      this.setState( {
        activeStep: activeStep + 1,
      } );
    }

  };

  handleBack = () => {
    this.setState( state => ({
      activeStep: state.activeStep - 1,
    }) );
  };

  handleReset = () => {
    this.setState( {
      activeStep: 0,
    } );
  };

  
  handleIndentificationStepChange = (identification) => {
    this.setState({identification});
  }

  handleAuthenticationStepChange = (authentication) => {
    this.setState({authentication});
  } 

  handleRoleStepChange = (roles) => {
    this.setState({roles});
  }

 
  render() {
    const {classes, vtnConfiguration} = this.props;
    const steps = getSteps();
    const {activeStep} = this.state;
    var that = this;


    function getStepContent( step ) {
      switch (step) {
        case 0:
          return <AccountUserCreateIdentificationStep classes={classes}
            identification={that.state.identification}
            onChange={that.handleIndentificationStepChange}
            vtnConfiguration={vtnConfiguration}/>;
        case 1:
          return <UserCreateAuthenticationFormPanel classes={classes}
            identification={that.state.identification}
            authentication={that.state.authentication}
            onChange={that.handleAuthenticationStepChange}/>;
        case 2:
          return <AccountCreateRoleStep classes={classes}
            roles={that.state.roles}
            onChange={that.handleRoleStepChange}/>;
        case 3:
          return <UserCreateConfirmationFormPanel classes={classes}
            identification={that.state.identification}
            authentication={that.state.authentication}
            roles={that.state.roles}/>;
        default:
          return t( 'common.unknownStep' );
      }
    }

    function getSetValidation( step ) {
      switch (step) {
        case 0:
          return true;
        case 1:
          return true;
        case 2:
          return true;
        case 3:
          return true;
        default:
          return false
      }
    }

    function handleValidatedNext( step ) {
      return (e) => {
        if ( getSetValidation( step ) ) {
          if ( that.state.hasError ) {
            that.setState( {
              hasError: false
            } );
          }
          that.handleNext( e );
        } else {
          that.setState( {
            hasError: true
          } );
        }
      }
    }

    return (
    <div className={ classes.root }>
      <Stepper activeStep={ activeStep } className={ classes.stepper }>
        { steps.map( (label, index) => {
            const props = {};
            const labelProps = {};
            props.completed = false;
            return (
            <Step key={ label } {...props}>
              <StepLabel {...labelProps}>
                { label }
              </StepLabel>
            </Step>
            );
          } ) }
      </Stepper>
      <div>
        { activeStep === steps.length ? (
            <div>
              <Typography className={ classes.instructions }>
                { t( 'common.allStepsCompleted' ) }
              </Typography>
              <Button onClick={ this.handleReset } className={ classes.button }>
                { t( 'common.reset' ) }
              </Button>
            </div>
            ) : (
            <div>
              <div>
                <Button disabled={ activeStep === 0 }
                        onClick={ this.handleBack }
                        className={ classes.button }>
                  { t( 'common.back' ) }
                </Button>
                <Button variant="contained"
                        color="primary"
                        onClick={ handleValidatedNext( activeStep ) }
                        className={ classes.button }>
                  { activeStep === steps.length - 1 ? t( 'accountCreate.createUser' ) : t( 'common.next' ) }
                </Button>
              </div>
              <Typography component="div" className={ classes.instructions }>
                <Paper elevation={ 1 } style={ { padding: '20px 0px' } }>
                  { getStepContent( activeStep ) }
                </Paper>
              </Typography>
              
            </div>
            ) }
      </div>
    </div>
    );
  }
}

export default AccountUserCreate;
