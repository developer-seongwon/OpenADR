import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';

import * as actions from '../../actions/vtnConfigurationActions';
import VtnConfigurationParameter from '../VtnConfiguration/VtnConfigurationParameter';
import VtnConfigurationMarketContext from '../VtnConfiguration/VtnConfigurationMarketContext';
import VtnConfigurationGroup from '../VtnConfiguration/VtnConfigurationGroup';

import { withStyles } from '@material-ui/core/styles';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';

import { history } from '../../store/configureStore';


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
    marginLeft: theme.spacing.unit,
    marginRight: theme.spacing.unit,
  },
  dense: {
    marginTop: 19,
  },
  menu: {
    width: 200,
  },
  formControl: {
    margin: theme.spacing.unit,
    minWidth: 500,
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
    margin: theme.spacing.unit,
  },
  iconButton: {
    marginTop: 10
  },
});

export class VtnConfigurationPage extends React.Component {
  state = {
    value: 0,
  };

  handleChange = (event, value) => {
    this.setState( {
      value
    } );
    switch(value) {
      case 0:
        history.push("/vtn_configuration/marketcontext")
        break;
      case 1:
        history.push("/vtn_configuration/group")
        break;
      case 2:
        history.push("/vtn_configuration/parameter")
        break;
      default:
        break;
    }
  };


  // 탭 순서. Tabs 에 그린 순서와 같아야 하고, App.js 의 :panel 라우트 목록과도 같아야 한다
  static PANELS = ['marketcontext', 'group', 'parameter'];

  panelIndex = (panel) => {
  	var i = VtnConfigurationPage.PANELS.indexOf(panel);
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
    this.props.actions.loadVtnConfiguration();
    this.props.actions.loadMarketContext();
    this.props.actions.loadGroup();
    this.setState({ value: this.panelIndex(this.props.match.params.panel) });
  }

  render() {
    const {classes, vtnConfiguration} = this.props;
    const {value} = this.state;
    return (
    <div className={ classes.root }>
      <Tabs value={ this.state.value }
            onChange={ this.handleChange }
            indicatorColor="primary"
            textColor="primary"
            centered>
        <Tab label="Market Contexts" />
        <Tab label="Groups" />
        <Tab label="Parameters" />
      </Tabs>
      <Divider variant="middle" />
      { value === 0 && <TabContainer>
                         <VtnConfigurationMarketContext classes={ classes }
                                                        marketContext={ vtnConfiguration.marketContext }
                                                        createMarketContext={ this.props.actions.createMarketContext }
                                                        updateMarketContext={ this.props.actions.updateMarketContext }
                                                        deleteMarketContext={ this.props.actions.deleteMarketContext } />
                       </TabContainer> }
      { value === 1 && <TabContainer>
                         <VtnConfigurationGroup classes={ classes }
                                                group={ vtnConfiguration.group }
                                                createGroup={ this.props.actions.createGroup }
                                                updateGroup={ this.props.actions.updateGroup }
                                                deleteGroup={ this.props.actions.deleteGroup } />
                       </TabContainer> }
      { value === 2 && <TabContainer>
                         <VtnConfigurationParameter classes={ classes } vtnConfiguration={ vtnConfiguration.parameters } />
                       </TabContainer> }
    </div>

    );
  }
}

VtnConfigurationPage.propTypes = {
  actions: PropTypes.object.isRequired
};

function mapStateToProps( state ) {
  return {
    vtnConfiguration: state.vtnConfiguration
  };
}

function mapDispatchToProps( dispatch ) {
  return {
    actions: bindActionCreators( actions, dispatch )
  };
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)( withStyles( styles )( VtnConfigurationPage ) );
