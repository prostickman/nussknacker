import React, { PropTypes, Component } from 'react';
import { render } from 'react-dom';
import { Scrollbars } from 'react-custom-scrollbars';
import { connect } from 'react-redux';
import _ from 'lodash'
import ActionsUtils from '../actions/ActionsUtils';
import '../stylesheets/processHistory.styl'

export class ProcessHistory_ extends Component {

  static propTypes = {
    processHistory: React.PropTypes.array.isRequired
  }

  constructor(props) {
    super(props);
    this.state = {
      currentProcess: {}
    };
  }

  componentWillReceiveProps(nextProps) {
    if (!_.isEqual(nextProps.processHistory, this.props.processHistory)) {
      this.resetState()
    }
  }

  resetState() {
    this.setState({currentProcess: {}})
  }

  showProcess(process, index) {
    this.setState({currentProcess: process})
    this.props.actions.fetchProcessToDisplay(process.processId, process.processVersionId)
  }

  processVersionOnTimeline(process, index) {
    if (_.isEmpty(this.state.currentProcess)) {
      return index == 0 ? "current" : "past"
    } else {
      return _.isEqual(process.createDate, this.state.currentProcess.createDate) ? "current" : process.createDate < this.state.currentProcess.createDate ? "past" : "";
    }
  }

  formatDate(date) {
    return date.substring(0, 16)
  }

  render() {
    return (
      <Scrollbars renderTrackHorizontal={props => <div className="hide"/>} autoHeight autoHeightMax={300} hideTracksWhenNotNeeded={true}>
        <ul id="process-history">
          {this.props.processHistory.map ((historyEntry, index) => {
            return (
              <li key={index} className={this.processVersionOnTimeline(historyEntry, index)}
                  onClick={this.showProcess.bind(this, historyEntry, index)}>
                {historyEntry.processName}:v{historyEntry.processVersionId} {historyEntry.user}
                <br/>
                <small><i>{this.formatDate(historyEntry.createDate)}</i></small>
                <br/>
                {historyEntry.deployments.map((deployment, index) =>
                  <small key={index}>{this.formatDate(deployment.deployedAt)} <span className="label label-info">{deployment.environment}</span></small>
                )}
              </li>
            )
          })}
        </ul>
      </Scrollbars>
    );
  }
}

function mapState(state) {
  return {
    processHistory: _.get(state.graphReducer.fetchedProcessDetails, 'history', []),
  };
}

export default connect(mapState, ActionsUtils.mapDispatchWithEspActions)(ProcessHistory_);