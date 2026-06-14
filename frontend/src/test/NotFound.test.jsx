import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import NotFound from '../pages/NotFound';

describe('NotFound', () => {
  function setup() {
    render(
      <MemoryRouter>
        <NotFound />
      </MemoryRouter>
    );
  }

  it('displays 404', () => {
    setup();
    expect(screen.getByText('404')).toBeInTheDocument();
  });

  it('displays error message', () => {
    setup();
    expect(screen.getByText('This page does not exist.')).toBeInTheDocument();
  });

  it('has a link to the dashboard', () => {
    setup();
    expect(screen.getByRole('link', { name: /go to dashboard/i })).toBeInTheDocument();
  });
});
