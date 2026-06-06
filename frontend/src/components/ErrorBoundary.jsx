import { Component } from 'react';

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: 12 }}>
          <h2 style={{ fontSize: 20, fontWeight: 700 }}>Something went wrong</h2>
          <button className="btn-primary" onClick={() => { this.setState({ hasError: false }); window.location.href = '/'; }}>
            Go to Dashboard
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
