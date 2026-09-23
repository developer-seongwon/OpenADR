import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';

import * as accountActions from '../actions/accountActions';


import { withStyles } from 'tss-react/mui';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';

import Avatar from '@mui/material/Avatar';


import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import FormControl from '@mui/material/FormControl';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Input from '@mui/material/Input';
import InputLabel from '@mui/material/InputLabel';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Paper from '@mui/material/Paper';
import { history, config } from '../store/configureStore';

import { amber } from '@mui/material/colors';



function TabContainer( props ) {
  return (
  <Typography component="div" style={ { padding: 8 * 3 } }>
    { props.children }
  </Typography>
  );
}

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  container: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  textField: {
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1),
  },
  dense: {
    marginTop: 19,
  },
  menu: {
    width: 200,
  },
  formControl: {
    margin: theme.spacing(1),
  },
  card: {
    maxWidth: 350,
    minWidth: 350,
  },
  media: {
    height: 40,
    paddingTop: 10,
    paddingRight: 10
  },
  button: {
    margin: theme.spacing(1),
  },
  stepper: {
    backgroundColor: '#fafafa'
  },
  warning: {
    backgroundColor: amber[ 700 ],
  },
  icon: {
    fontSize: 20,
  },
  iconVariant: {
    opacity: 0.9,
    marginRight: theme.spacing(1),
  },
  message: {
    display: 'flex',
  },
  iconButton: {
    marginTop: 10
  },
  main: {
    width: 'auto',
    display: 'block', // Fix IE 11 issue.
    marginLeft: theme.spacing(3),
    marginRight: theme.spacing(3),
    // MUI 3 의 theme.spacing.unit 은 숫자(8)였고 MUI 5 부터 theme.spacing(3) 은 '24px' 문자열이다.
    // 그대로 곱하면 NaN 이 되어 이 breakpoint 가 통째로 무시된다. 좌우 여백 24px 두 번을 숫자로 적는다
    [theme.breakpoints.up(400 + 24 * 2)]: {
      width: 400,
      marginLeft: 'auto',
      marginRight: 'auto',
    },
  },
  paper: {
    marginTop: theme.spacing(8),
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    padding: `${theme.spacing(2)} ${theme.spacing(3)} ${theme.spacing(3)}`,
  },
  avatar: {
    margin: theme.spacing(1),
    backgroundColor: theme.palette.secondary.main,
  },
  form: {
    width: '100%', // Fix IE 11 issue.
    marginTop: theme.spacing(1),
  },
  submit: {
    marginTop: theme.spacing(3),
  },
});

export class LoginPage extends React.Component {
  state = {
    value: 0,
    username:"",
    password:""
  };

  handleChange = (event, value) => {
    this.setState( {
      value
    } );
  };


  componentDidMount() {
    this.props.accountActions.loadLoginUser();
  }

  // componentWillReceiveProps 는 React 16.3 부터 쓰지 말라는 옛 생명주기다(React 18 개발 모드에서 경고).
  // 여기서 하는 일은 props 로 state 를 만드는 게 아니라 로그인 뒤 이동이라 componentDidUpdate 가 맞다.
  // 예전처럼 user 가 바뀌었을 때만 보고, 로그인 상태면 원래 가려던 곳으로 보낸다
  componentDidUpdate(prevProps) {
    if(this.props.user !== prevProps.user && this.props.user.isConnected){
      if(this.props.history.location && this.props.history.location.state 
          && this.props.history.location.state.from) {
        history.push( this.props.history.location.state.from.pathname)
      }
      else {
        history.push( '/' )
      }
    }
  }

  handleUsernameChange = (e) => {
    this.setState({username: e.target.value});
  }

  handlePasswordChange = (e) => {
    this.setState({password: e.target.value});
  }

  handleSubmit = () => {
    config.username = this.state.username;
    config.password = this.state.password;
    this.setState({username: "", password: ""});
    this.props.accountActions.loadLoginUser();
  }

  render() {
    const {classes} = this.props;
    const {value} = this.state;

    // 인증 실패인지 상태 코드로 판정한다.
    //
    // 예전에는 에러 문자열을 "Error: Forbidden" 과 글자 그대로 비교했다.
    // 그 문구는 HTTP 응답의 reason phrase 에서 오는데, Jetty 는 "Forbidden" 을 붙여 보냈지만
    // 톰캣은 붙이지 않는다. 그러면 에러가 그냥 "Error" 가 되어 비교가 빗나가고,
    // 로그인 폼 대신 "Can't connect to VTN backend" 만 뜬다.
    // 문구는 서버 구현에 따라 달라지니 상태 코드를 본다. 옛 비교도 남겨 둔다.
    var connectionError = this.props.user.connectionError;
    var hasError = connectionError != null;
    var errorStatus = connectionError
        && ( connectionError.status
          || ( connectionError.response && connectionError.response.status ) );
    var authenticationError = hasError
        && ( errorStatus === 401 || errorStatus === 403
          || ""+connectionError === "Error: Forbidden"
          || ""+connectionError === "Error: Unauthorized")

    return (
    <div className={ classes.root }>
      {(!hasError) ? <div>
          <Tabs value={ this.state.value }
            onChange={ this.handleChange }
            indicatorColor="primary"
            textColor="primary"
            centered>
        <Tab label="Login" />
      </Tabs>
      <Divider variant="middle" />

      { value === 0 && <TabContainer>
          
                       </TabContainer> }

        </div>: null }

      {(hasError && authenticationError) ? <div>
        {/* "SSL Certificate authentication failed, must provide login / password"*/ }
          <main className={classes.main}>
      <CssBaseline />
           <Paper className={classes.paper}>
        <Avatar className={classes.avatar}>
          <LockOutlinedIcon />
        </Avatar>
        <Typography component="h1" variant="h5">
          Sign in
        </Typography>
        <form className={classes.form}>
          <FormControl margin="normal" required fullWidth>
            <InputLabel htmlFor="username">Username</InputLabel>
            <Input autoComplete="username" autoFocus 
              onChange={this.handleUsernameChange}/>
          </FormControl>
          <FormControl margin="normal" required fullWidth>
            <InputLabel htmlFor="password">Password</InputLabel>
            <Input id="password" autoComplete="current-password"
              onChange={this.handlePasswordChange}/>
          </FormControl>
          <FormControlLabel
            control={<Checkbox value="remember" color="primary" />}
            label="Remember me"
          />
          <Button
            fullWidth
            variant="contained"
            color="primary"
            className={classes.submit}
            onClick={this.handleSubmit}
          >
            Sign in
          </Button>
        </form>
      </Paper>
        </main>
        </div>: null }

      {(hasError && !authenticationError) ? <div>
        { "Can't connect to VTN backend:" + this.props.user.connectionError}
        </div>: null }
        
      

    </div>

    );
  }
}

LoginPage.propTypes = {
  accountActions: PropTypes.object.isRequired
};

function mapStateToProps( state ) {
  return {
    user: state.user
  };
}

function mapDispatchToProps( dispatch ) {
  return {
    accountActions: bindActionCreators( accountActions, dispatch ),
  };
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)( withStyles(LoginPage, styles) );
