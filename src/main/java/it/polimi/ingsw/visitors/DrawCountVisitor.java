package it.polimi.ingsw.visitors;

import it.polimi.ingsw.model.entities.card.effects.interactive.DrawCard;

/**
 * Visitor that accumulates the total number of upper- and lower-row draw
 * actions granted by {@link DrawCard} effects.
 *
 * <p>Call {@link #visit(DrawCard)} once per effect, then read the aggregated
 * counts via {@link #getUpDraws()} and {@link #getDownDraws()}.</p>
 */
public class DrawCountVisitor {
    /**
     * Accumulates the total count of upper-row draw actions performed by
     * {@link DrawCard} effects in the current visitor session.
     * <p>
     * The value is incremented with each call to {@link DrawCountVisitor#visit(DrawCard)},
     * and it represents the cumulative number of upper-row draws up to the
     * current point.
     * </p>
     */
    private int upDraws;
    /**
     * Accumulates the total count of lower-row draw actions performed by
     * {@link DrawCard} effects in the current visitor session.
     * The value is incremented with each call to {@link DrawCountVisitor#visit(DrawCard)},
     * and it represents the cumulative number of lower-row draws up to the
     * current point.
     */
    private int downDraws;

    /**
     * Constructs a new {@code DrawCountVisitor} with both counters set to zero.
     */
    public DrawCountVisitor() {
        this.upDraws = 0;
        this.downDraws = 0;
    }

    /**
     * Accumulates the draw counts of the given {@link DrawCard} effect into
     * this visitor's running totals.
     *
     * @param effect the {@link DrawCard} effect to visit; must not be {@code null}
     */
    public void visit(DrawCard effect) {
        this.upDraws += effect.getUpDraws();
        this.downDraws += effect.getDownDraws();
    }

    /**
     * Returns the total number of upper-row draws accumulated so far.
     *
     * @return total upper-row draw count; {@code 0} if no effects have been visited
     */
    public int getUpDraws() {
        return upDraws;
    }

    /**
     * Returns the total number of lower-row draws accumulated so far.
     *
     * @return total lower-row draw count; {@code 0} if no effects have been visited
     */
    public int getDownDraws() {
        return downDraws;
    }
}