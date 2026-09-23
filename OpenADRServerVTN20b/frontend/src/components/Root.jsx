 import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { ConnectedRouter } from 'connected-react-router';
import { Provider } from 'react-redux';
import App from './App';

/**
 * @deprecated 어디서도 import 하지 않는다. index.jsx 가 같은 역할을 직접 한다.
 * connected-react-router 를 걷어내면서 이 파일의 import 도 깨진 상태로 남아 있다(번들에는 안 들어간다).
 */
export default class Root extends Component {
  render() {
    const {store, history} = this.props;
    return (
    <Provider store={ store }>
      <ConnectedRouter history={ history }>
        <App />
      </ConnectedRouter>
    </Provider>
    );
  }
}

Root.propTypes = {
  store: PropTypes.object.isRequired,
  history: PropTypes.object.isRequired
};
