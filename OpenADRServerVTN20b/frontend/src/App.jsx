/* eslint-disable import/no-named-as-default */
import { Routes, Route, Navigate, useLocation, useParams } from 'react-router';

import LoginPage from './components/LoginPage';
import AboutPage from './components/AboutPage';
import HomePage from './components/HomePage';
import NotFoundPage from './components/NotFoundPage';
import VtnConfigurationPage from './components/containers/VtnConfigurationPage';
import VenPage from './components/containers/VenPage'
import AccountPage from './components/containers/AccountPage'
import AccountUserCreatePage from './components/containers/AccountUserCreatePage'
import AccountAppCreatePage from './components/containers/AccountAppCreatePage'
import EventPage from './components/containers/EventPage'
import EventDetailPage from './components/containers/EventDetailPage'
import EventCreatePage from './components/containers/EventCreatePage'
import VenDetailPage from './components/containers/VenDetailPage'
import VenDetailCreateReportPage from './components/containers/VenDetailCreateReportPage'
import VenDetailReportPage from './components/containers/VenDetailReportPage'
import VenDetailReportRequestPage from './components/containers/VenDetailReportRequestPage'


import VenCreatePage from './components/containers/VenCreatePage'

import PropTypes from 'prop-types';
import React from 'react';



import MenuItem from '@mui/material/MenuItem';
import Menu from '@mui/material/Menu';


import classNames from 'classnames';
import { withStyles } from 'tss-react/mui';
import CssBaseline from '@mui/material/CssBaseline';
import Drawer from '@mui/material/Drawer';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import List from '@mui/material/List';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';

import MenuIcon from '@mui/icons-material/Menu';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';

import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';


import NavigationMain from './components/Navigation';



import { history, config } from './store/configureStore';



const drawerWidth = 240;

const styles = theme => ({
  root: {
    display: 'flex',
  },
  toolbar: {
    paddingRight: 24, // keep right padding when drawer closed
  },
  toolbarIcon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: '0 8px',
    ...theme.mixins.toolbar,
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
    transition: theme.transitions.create( [ 'width', 'margin' ], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    } ),
  },
  appBarShift: {
    marginLeft: drawerWidth,
    width: `calc(100% - ${drawerWidth}px)`,
    transition: theme.transitions.create( [ 'width', 'margin' ], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    } ),
  },
  menuButton: {
    marginLeft: 12,
    marginRight: 36,
  },
  menuButtonHidden: {
    display: 'none',
  },
  title: {
    flexGrow: 1,
  },
  drawerPaper: {
    position: 'relative',
    whiteSpace: 'nowrap',
    width: drawerWidth,
    transition: theme.transitions.create( 'width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    } ),
  },
  drawerPaperClose: {
    overflowX: 'hidden',
    transition: theme.transitions.create( 'width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    } ),
    width: theme.spacing(7),
    [ theme.breakpoints.up( 'sm' )]: {
      width: theme.spacing(9),
    },
  },
  appBarSpacer: theme.mixins.toolbar,
  content: {
    flexGrow: 1,
    padding: theme.spacing(3),
    height: '100vh',
    overflow: 'auto',
  },
  chartContainer: {
    marginLeft: -22,
  },
  tableContainer: {
    height: 320,
  },
  h5: {
    marginBottom: theme.spacing(2),
  },
});

// react-router 4 는 <Route component> 로 그린 컴포넌트에 match, location, history 를 넣어 줬다.
// 화면들이 this.props.match.params 를 51군데서 읽는데, 라우터 7 은 이 props 를 주지 않고 훅만 준다.
// 클래스 컴포넌트가 64개라 전부 훅으로 바꾸는 대신 여기서 같은 모양으로 넣어 준다.
// params 는 경로에서 뽑은 값에 fixedParams 를 덮는다. 아래 panelRoutes 참고
function RouteProps( { component: Component, fixedParams } ) {
  const params = useParams();
  const location = useLocation();
  const match = { params: { ...params, ...fixedParams } };
  return <Component match={ match } location={ location } history={ history } />;
}

// 예전 PrivateRoute. 로그인 전이면 /login 으로 보내고 원래 가려던 위치를 state.from 에 싣는다.
// LoginPage 가 로그인 뒤 그 위치로 돌려보낸다.
// 예전 <Redirect> 는 push 옵션이 없으면 replace 였다. 똑같이 replace 로 보낸다
function RequireAuth( { children } ) {
  const location = useLocation();
  if ( !config.isConnected ) {
    return <Navigate to="/login" replace state={ { from: location } } />;
  }
  return children;
}

function publicRoute( path, Component ) {
  return <Route key={ path } path={ path } element={ <RouteProps component={ Component } /> } />;
}

function privateRoute( path, Component, fixedParams ) {
  return (
    <Route key={ path }
           path={ path }
           element={ <RequireAuth><RouteProps component={ Component } fixedParams={ fixedParams } /></RequireAuth> } />
  );
}

// 라우터 4 는 /account/:panel(user|app) 처럼 파라미터에 정규식을 걸 수 있었는데 6 부터 없어졌다.
// :panel 로 열어 두면 /account/foo 같은 주소도 받아 버려서, 허용된 값마다 경로를 하나씩 만들고
// panel 값은 fixedParams 로 넣는다. 화면은 예전처럼 match.params.panel 로 읽는다
function panelRoutes( base, Component, panels ) {
  return panels.map( ( panel ) => privateRoute( base + '/' + panel, Component, { panel } ) );
}

// This is a class-based component because the current
// version of hot reloading won't hot reload a stateless
// component at the top-level.

class App extends React.Component {

  constructor(props) {
    super(props)
    this.state = {
      open: true,
      anchorEl: null, 
      config: null
    };

  }
  
  handleDrawerOpen = () => {
    this.setState( {
      open: true
    } );
  };

  handleDrawerClose = () => {
    this.setState( {
      open: false
    } );
  };

  handleProfileMenuOpen = event => {
    this.setState({ anchorEl: event.currentTarget });
  };

  handleMenuClose = () => {
    this.setState({ anchorEl: null });
  };

  handleSignOut = () => {
    this.handleMenuClose();
    config.username = null;
    config.password = null;
    config.user = null;
    config.isConnected = false;
    config.isConnectionPending = false;
    history.push("/login")
  }


  render() {
    const {classes} = this.props;
    const { anchorEl } = this.state;
    const isMenuOpen = Boolean(anchorEl);
    const renderMenu = (
      <Menu
        anchorEl={anchorEl}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        open={isMenuOpen}
        onClose={this.handleMenuClose}
      >
        <MenuItem key="menu_items_profile" onClick={this.handleMenuClose}>Profile</MenuItem>
        <MenuItem key="menu_items_myaccount" onClick={this.handleMenuClose}>My account</MenuItem>


        {config.user && config.user.roles.map(row => (
           <MenuItem key={"menu_items"+row}>{row}</MenuItem>
        ))}
        <MenuItem key="menu_items_signout" onClick={this.handleSignOut}>Sign Out</MenuItem>
      </Menu>
    );

    return (
      <div className={ classes.root }>
        <CssBaseline />
        <AppBar position="absolute" className={ classNames( classes.appBar, this.state.open && classes.appBarShift ) }>
          <Toolbar disableGutters={ !this.state.open } className={ classes.toolbar }>
            <IconButton
              color="inherit"
              aria-label="Open drawer"
              onClick={ this.handleDrawerOpen }
              className={ classNames(
                            classes.menuButton,
                            this.state.open && classes.menuButtonHidden,
                          ) }
              size="large">
              <MenuIcon />
            </IconButton>
            <Typography component="h1"
                        variant="h6"
                        noWrap
                        className={ classes.title }
                        sx={{
                          color: "inherit"
                        }}>
              VTN Control
            </Typography>
          {/* 
            <IconButton color="inherit">
              <Badge badgeContent={ 4 } color="secondary">
               {(config.isConnected) ? config.user.username: ""} <AccountBoxIcon />
              </Badge>
            </IconButton>
            */}
            <IconButton
              aria-owns={isMenuOpen ? 'material-appbar' : undefined}
              aria-haspopup="true"
              onClick={this.handleProfileMenuOpen}
              color="inherit"
              size="large">
                  <AccountCircleIcon />
                </IconButton>
          </Toolbar>
        </AppBar>
        {renderMenu}
        <Drawer variant="permanent"
                classes={ { paper: classNames( classes.drawerPaper, !this.state.open && classes.drawerPaperClose ), } }
                open={ this.state.open }>
          <div className={ classes.toolbarIcon }>
            <IconButton onClick={ this.handleDrawerClose } size="large">
              <ChevronLeftIcon />
            </IconButton>
          </div>
          <Divider />
          <List>
            <NavigationMain classes={classes}/>
          </List>
          <Divider />
          <List>

          </List>
        </Drawer>
        <main className={ classes.content }>
          <div className={ classes.appBarSpacer } />
          {(config.connectionError == null) ? <Routes>
            { privateRoute( '/', HomePage ) }

            { publicRoute( '/login', LoginPage ) }
            { publicRoute( '/about', AboutPage ) }

            { privateRoute( '/account/app/create', AccountAppCreatePage ) }
            { privateRoute( '/account/user/create', AccountUserCreatePage ) }
            { panelRoutes( '/account', AccountPage, [ 'user', 'app' ] ) }
            { privateRoute( '/account', AccountPage ) }

            { panelRoutes( '/vtn_configuration', VtnConfigurationPage, [ 'marketcontext', 'group', 'parameter' ] ) }
            { privateRoute( '/vtn_configuration', VtnConfigurationPage ) }

            { privateRoute( '/ven/detail/:username/reports/:reportSpecifierId/requests/:reportRequestId', VenDetailReportRequestPage ) }
            { privateRoute( '/ven/detail/:username/reports/:reportSpecifierId/create', VenDetailCreateReportPage ) }
            { privateRoute( '/ven/detail/:username/reports/:reportSpecifierId', VenDetailReportPage ) }
            {/* VenDetailPage 의 탭은 여섯 개인데 여기에 세 개만 적혀 있었다.
                requests, enrollments, groups 로 새로고침하거나 주소를 직접 열면
                panel 이 undefined 가 되어 Settings 탭이 열렸다.
                탭을 추가하면 여기도 같이 넣어야 한다. */}
            { panelRoutes( '/ven/detail/:username', VenDetailPage,
                [ 'settings', 'reports', 'requests', 'optschedules', 'enrollments', 'groups' ] ) }
            { privateRoute( '/ven/detail/:username', VenDetailPage ) }
            { privateRoute( '/ven/create', VenCreatePage ) }
            { privateRoute( '/ven', VenPage ) }

            { panelRoutes( '/event/detail/:id', EventDetailPage,
                [ 'descriptor', 'activeperiod', 'signal', 'target', 'venresponse' ] ) }
            { privateRoute( '/event/detail/:id', EventDetailPage ) }
            { privateRoute( '/event/create', EventCreatePage ) }
            { panelRoutes( '/event', EventPage, [ 'list', 'calendar' ] ) }
            { privateRoute( '/event', EventPage ) }

            {/* <Switch> 안에 AccountAppCreatePage 라는 글자가 그냥 적혀 있었다. <Routes> 는
                Route 가 아닌 자식을 받으면 에러를 내서 지웠다 */}
            { publicRoute( '*', NotFoundPage ) }
          </Routes>: null}


          {(config.connectionError != null) ? <div>
            {console.log(config.connectionError)}
          </div>: null}
          
          
          
        </main>
      </div>
    );
  }
}

App.propTypes = {
  children: PropTypes.element
};

export default withStyles(App, styles);




