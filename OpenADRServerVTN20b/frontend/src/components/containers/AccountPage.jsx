import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';

import * as accountActions from '../../actions/accountActions';

import { withStyles } from 'tss-react/mui';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';

import AccountUser from '../Account/AccountUser'


import AccountApp from '../Account/AccountApp'
import { history } from '../../store/configureStore';

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
});

export class AccountPage extends React.Component {
  state = {
    value: 0,
  };

  handleChange = (event, value) => {
    this.setState( {
      value
    } );
    switch(value) {
      case 0:
        history.push("/account/user")
        break;
      case 1:
        history.push("/account/app")
        break;
      default:
        break;
    }
  };


  // 탭 순서. Tabs 에 그린 순서와 같아야 하고, App.js 의 :panel 라우트 목록과도 같아야 한다
  static PANELS = ['user', 'app'];

  panelIndex = (panel) => {
  	var i = AccountPage.PANELS.indexOf(panel);
  	return i < 0 ? 0 : i;
  }

  /**
   * 주소가 다른 패널로 바뀌면 탭도 따라가야 한다.
   *
   * 예전에는 componentDidMount 에서만 탭을 정했다. 컴포넌트는 패널만 바뀔 때
   * 다시 마운트되지 않으므로, 브라우저 뒤로가기나 패널 주소를 직접 여는 경우
   * 주소는 바뀌는데 탭은 그대로 첫 번째에 머물렀다.
   */
  componentDidUpdate(prevProps) {
  	if (prevProps.match.params.panel !== this.props.match.params.panel) {
  		this.setState({ value: this.panelIndex(this.props.match.params.panel) });
  	}
  }

  componentDidMount() {
    this.props.accountActions.loadAccountUser();
    this.props.accountActions.loadAccountApp();
    this.setState({ value: this.panelIndex(this.props.match.params.panel) });
  }


  render() {
    const {classes, account} = this.props;
    const {value} = this.state;
    return (
    <div className={ classes.root }>
      <Tabs value={ this.state.value }
            onChange={ this.handleChange }
            indicatorColor="primary"
            textColor="primary"
            centered>
        <Tab label="Users" />
        <Tab label="Apps" />
      </Tabs>
      <Divider variant="middle" />
      { value === 0 && <TabContainer>
                        <AccountUser classes={classes} user={account.user} deleteUser={this.props.accountActions.deleteUser}/>
                       </TabContainer> }
      { value === 1 && <TabContainer>
                          <AccountApp classes={classes} app={account.app} deleteApp={this.props.accountActions.deleteApp}/>
                       </TabContainer> }
    </div>

    );
  }
}

AccountPage.propTypes = {
  accountActions: PropTypes.object.isRequired
};

function mapStateToProps( state ) {
  return {
    account: state.account
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
)( withStyles(AccountPage, styles) );
