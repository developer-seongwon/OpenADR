import React from 'react';
import PropTypes from 'prop-types';
import Autosuggest from 'react-autosuggest';
import TextField from '@mui/material/TextField';
import Paper from '@mui/material/Paper';
import MenuItem from '@mui/material/MenuItem';
import { withStyles } from 'tss-react/mui';

function renderInputComponent(inputProps) {
  const { classes, inputRef = () => {}, ref, ...other } = inputProps;

  return (
    <TextField
      fullWidth
      {...other}
      slotProps={{
        input: {
          inputRef: node => {
            ref(node);
            inputRef(node);
          },
          classes: {
            input: classes.input,
          },
        }
      }} />
  );
}


const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  container: {
    position: 'relative',
  },
  suggestionsContainerOpen: {
    position: 'absolute',
    zIndex: 1,
    marginTop: theme.spacing(1),
    left: 0,
    right: 0,
  },
  suggestion: {
    display: 'block',
  },
  suggestionsList: {
    margin: 0,
    padding: 0,
    listStyleType: 'none',
  },
  divider: {
    height: theme.spacing(2),
  },
});


export class VenAutocomplete extends React.Component {
  state = {
    single: '',
    popper: '',
  };


  handleChange = name => (event, { newValue }) => {
    this.setState({
      [name]: newValue,
    });
  };

  render() {
    const { classes } = this.props;

    var that = this;

    const autosuggestProps = {
      renderInputComponent,
      suggestions: this.props.suggestions,
      onSuggestionsFetchRequested: this.props.onSuggestionsFetchRequested,
      onSuggestionsClearRequested: this.props.onSuggestionsClearRequested,
      onSuggestionsSelect: this.props.onSuggestionsSelect,
      getSuggestionValue,
      renderSuggestion,
    };


    function getSuggestionValue(suggestion) {
      return suggestion.commonName;
    }


    function renderSuggestion(suggestion) {
      return (
        <MenuItem component="div" onClick={() => {that.props.onSuggestionsSelect(suggestion)}}>
          <div>
            <strong>{suggestion.commonName}</strong> (<i>{suggestion.username}</i>)
          </div>
        </MenuItem>
      );
    }


    return (
      <div className={classes.root}>
         <Autosuggest
          {...autosuggestProps}
          inputProps={{
            classes,
            label: "venId, commonName, oadrName",
            placeholder: '',
            value: this.state.single,
            onChange: this.handleChange('single'),
          }}

          theme={{
            container: classes.container,
            suggestionsContainerOpen: classes.suggestionsContainerOpen,
            suggestionsList: classes.suggestionsList,
            suggestion: classes.suggestion,
          }}
          renderSuggestionsContainer={options => (
            <Paper {...options.containerProps} square>
              {options.children}
            </Paper>
          )}
        />
      </div>
    );
  }
}

VenAutocomplete.propTypes = {
  classes: PropTypes.object.isRequired,
};

VenAutocomplete = withStyles(VenAutocomplete, styles);

export class EventAutocomplete extends React.Component {
  state = {
    single: '',
    popper: '',
  };


  handleChange = name => (event, { newValue }) => {
    this.setState({
      [name]: newValue,
    });
  };

  render() {
    const { classes } = this.props;

    var that = this;

    const autosuggestProps = {
      renderInputComponent,
      suggestions: this.props.suggestions,
      onSuggestionsFetchRequested: this.props.onSuggestionsFetchRequested,
      onSuggestionsClearRequested: this.props.onSuggestionsClearRequested,
      onSuggestionsSelect: this.props.onSuggestionsSelect,
      getSuggestionValue,
      renderSuggestion,
    };


    function getSuggestionValue(suggestion) {
      return suggestion.descriptor.eventId;
    }


    function renderSuggestion(suggestion) {
      return (
        <MenuItem component="div" onClick={() => {that.props.onSuggestionsSelect(suggestion)}}>
          <div>
            <strong>{suggestion.descriptor.eventId}</strong>
          </div>
        </MenuItem>
      );
    }


    return (
      <div className={classes.root}>
         <Autosuggest
          {...autosuggestProps}
          inputProps={{
            classes,
            placeholder: '',
            value: this.state.single,
            onChange: this.handleChange('single'),
          }}
          theme={{
            container: classes.container,
            suggestionsContainerOpen: classes.suggestionsContainerOpen,
            suggestionsList: classes.suggestionsList,
            suggestion: classes.suggestion,
          }}
          renderSuggestionsContainer={options => (
            <Paper {...options.containerProps} square>
              {options.children}
            </Paper>
          )}
        />
      </div>
    );
  }
}

EventAutocomplete.propTypes = {
  classes: PropTypes.object.isRequired,
};

EventAutocomplete = withStyles(EventAutocomplete, styles);