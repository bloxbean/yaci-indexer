package com.bloxbean.cardano.yaci.store.governanceaggr.util;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionId;
import com.bloxbean.cardano.yaci.core.model.governance.GovActionType;
import com.bloxbean.cardano.yaci.store.governanceaggr.domain.Proposal;

import java.util.*;

public class ProposalUtils {

    /**
     * Finds all descendants and siblings of a specific proposal.
     *
     * @param proposal     The proposal for which descendants and/or siblings are to be found.
     * @param allProposals The list of all proposals.
     * @return A list of proposals that are descendants and siblings of the given proposal.
     */
    public static List<Proposal> findDescendantsAndSiblings(Proposal proposal, List<Proposal> allProposals) {
        // Handle special types that have no siblings or descendants
        if (proposal.getType() == GovActionType.INFO_ACTION || proposal.getType() == GovActionType.TREASURY_WITHDRAWALS_ACTION) {
            return Collections.emptyList();
        }

        List<Proposal> result = new ArrayList<>();
        GovActionType type = proposal.getType();

        // Add descendants
        result.addAll(findDescendants(proposal, allProposals, type));

        GovActionId parentId = proposal.getPreviousGovActionId();
        if (parentId != null) {
            // Find siblings with the same parent and type
            result.addAll(
                    allProposals.stream()
                            .filter(p -> parentId.equals(p.getPreviousGovActionId()) && !p.equals(proposal) && type == p.getType())
                            .toList()
            );
        } else {
            // If no parent, find siblings among root nodes
            result.addAll(
                    allProposals.stream()
                            .filter(p -> p.getPreviousGovActionId() == null && !p.equals(proposal) && type == p.getType())
                            .toList()
            );
        }

        return result;
    }

    /**
     * Find siblings of a proposal
     * @param proposal
     * @param allProposals
     * @return
     */
    public static List<Proposal> findSiblings(Proposal proposal, List<Proposal> allProposals) {
        GovActionType type = proposal.getType();

        if (proposal.getType() == GovActionType.INFO_ACTION || proposal.getType() == GovActionType.TREASURY_WITHDRAWALS_ACTION) {
            return Collections.emptyList();
        }

        GovActionId parentId = proposal.getPreviousGovActionId();

        if (parentId == null) {
            return allProposals.stream()
                    .filter(p -> p.getPreviousGovActionId() == null && !p.equals(proposal) && type == p.getType())
                    .toList();
        } else {
            return allProposals.stream()
                    .filter(p -> parentId.equals(p.getPreviousGovActionId()) && !p.equals(proposal) && type == p.getType())
                    .toList();
        }
    }

    /**
     * Find descendants of a proposal
     * @param proposal
     * @param allProposals
     * @return
     */
    public static List<Proposal> findDescendants(Proposal proposal, List<Proposal> allProposals) {
        GovActionType type = proposal.getType();

        return new ArrayList<>(findDescendants(proposal, allProposals, type));
    }

    private static List<Proposal> findDescendants(Proposal rootProposal, List<Proposal> allProposals, GovActionType type) {
        if (type == GovActionType.INFO_ACTION || type == GovActionType.TREASURY_WITHDRAWALS_ACTION) {
            return Collections.emptyList();
        }

        List<Proposal> descendants = new ArrayList<>();
        Queue<Proposal> queue = new LinkedList<>();
        queue.add(rootProposal);

        while (!queue.isEmpty()) {
            Proposal current = queue.poll();
            List<Proposal> children = allProposals.stream()
                    .filter(p -> current.getGovActionId().equals(p.getPreviousGovActionId()) && type.equals(p.getType()))
                    .toList();

            descendants.addAll(children);
            queue.addAll(children);
        }

        return descendants;
    }
}
