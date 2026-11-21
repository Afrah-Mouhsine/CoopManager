import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import StatusBadge from '../components/StatusBadge'

describe('StatusBadge', () => {
  it('renders PENDING correctly', () => {
    render(<StatusBadge status="PENDING" />)
    expect(screen.getByText('En attente')).toBeInTheDocument()
  })

  it('renders VALIDATED correctly', () => {
    render(<StatusBadge status="VALIDATED" />)
    expect(screen.getByText('Validé')).toBeInTheDocument()
  })

  it('renders REJECTED correctly', () => {
    render(<StatusBadge status="REJECTED" />)
    expect(screen.getByText('Rejeté')).toBeInTheDocument()
  })

  it('applies correct class for PENDING', () => {
    const { container } = render(<StatusBadge status="PENDING" />)
    expect(container.firstChild).toHaveClass('badge-pending')
  })

  it('applies correct class for VALIDATED', () => {
    const { container } = render(<StatusBadge status="VALIDATED" />)
    expect(container.firstChild).toHaveClass('badge-validated')
  })
})
